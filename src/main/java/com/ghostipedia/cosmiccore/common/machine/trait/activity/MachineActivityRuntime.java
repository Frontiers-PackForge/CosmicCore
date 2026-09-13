package com.ghostipedia.cosmiccore.common.machine.trait.activity;

import com.ghostipedia.cosmiccore.api.machine.activity.ActivityScope;
import com.ghostipedia.cosmiccore.api.machine.activity.MachineActivity;
import com.ghostipedia.cosmiccore.api.machine.activity.MachineActivitySource;
import com.ghostipedia.cosmiccore.api.machine.activity.ProductionBindingSource;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.MultithreadedMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.MultithreadedRecipeLogic;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

public final class MachineActivityRuntime {

    private MachineActivityRuntime() {}

    public static MachineActivity activity(MetaMachine machine) {
        if (!(machine.getLevel() instanceof ServerLevel) || !(machine instanceof MachineActivitySource source))
            return null;
        return source.cosmiccore$activity();
    }

    public static ActivityScope scope(RecipeLogic logic) {
        return new ActivityScope(activity(logic.getMachine()), lane(logic), logic.getMachine());
    }

    public static ActivityScope scope(MetaMachine machine) {
        return new ActivityScope(activity(machine), 0, machine);
    }

    public static ActivityScope scope(IRecipeCapabilityHolder holder, boolean simulated) {
        if (simulated) return ActivityScope.suspend();
        if (holder instanceof RecipeLogic logic) return scope(logic);
        if (holder instanceof MetaMachine machine) {
            ActivityScope current = ActivityScope.current();
            MachineActivity activity = activity(machine);
            int lane = current != null && current.activity() == activity ? current.lane() : 0;
            return new ActivityScope(activity, lane, machine);
        }
        return ActivityScope.suspend();
    }

    public static FluidStack recordedInput(FluidStack extracted, FluidAction action) {
        if (action.execute()) ActivityScope.fluid(extracted, extracted.getAmount(), true);
        return extracted;
    }

    public static ItemStack recordedInput(ItemStack extracted, boolean simulated) {
        if (!simulated) ActivityScope.item(extracted, extracted.getCount(), true);
        return extracted;
    }

    public static void tick(MetaMachine machine) {
        if (machine.getLevel() instanceof ServerLevel && machine instanceof ProductionBindingSource source)
            source.cosmiccore$productionBinding().pool(machine);
        MachineActivity activity = activity(machine);
        if (activity == null || !(machine instanceof IRecipeLogicMachine)) return;
        activity.tick(machine.getLevel().getGameTime(), machine.getLevel().registryAccess());
    }

    public static void states(MetaMachine machine) {
        MachineActivity activity = activity(machine);
        if (activity == null) return;
        if (machine instanceof MultithreadedMachine multithreaded) {
            for (MultithreadedRecipeLogic logic : multithreaded.getThreadLogics().values())
                activity.state(lane(logic), logic.getStatus().getSerializedName(), logic.getProgress());
        } else if (machine instanceof IRecipeLogicMachine workable) {
            RecipeLogic logic = workable.getRecipeLogic();
            activity.state(0, logic.getStatus().getSerializedName(), logic.getProgress());
        }
    }

    public static int lane(RecipeLogic logic) {
        return logic instanceof MultithreadedRecipeLogic thread ? thread.getThreadIndex() : 0;
    }

    public static void started(RecipeLogic logic, GTRecipe recipe) {
        MachineActivity activity = activity(logic.getMachine());
        if (activity != null) activity.begin(lane(logic), recipe.getId().toString(), recipe.duration);
    }

    public static void completed(RecipeLogic logic, ActionResult result) {
        MachineActivity activity = activity(logic.getMachine());
        if (activity == null) return;
        if (result.isSuccess()) activity.complete(lane(logic));
        else {
            failure(logic, result);
            activity.interrupt(lane(logic));
        }
    }

    public static void failure(RecipeLogic logic, ActionResult result) {
        if (result.isSuccess()) return;
        MachineActivity activity = activity(logic.getMachine());
        if (activity == null) return;
        String reason = result.io() == IO.OUT ? "output" : result.io() == IO.IN ?
                result.capability() == EURecipeCapability.CAP ? "power" : "input" : "condition";
        activity.failure(lane(logic), reason);
    }
}
