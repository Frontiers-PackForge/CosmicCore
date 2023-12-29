package com.ghostipedia.cosmiccore.gtbridge.machine.kinetic;

import com.ghostipedia.cosmiccore.gtbridge.BasicSteamTurbineLogic;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.StressRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.blockentity.KineticMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.kinetic.IKineticMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.KineticPartMachine;
import com.gregtechceu.gtceu.common.machine.trait.NotifiableStressTrait;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.misc.FluidTransferList;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author KilaBash
 * @date 2023/7/9
 * @implNote LargeCombustionEngineMachine
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BasicSteamTurbineMachine extends WorkableElectricMultiblockMachine implements IDisplayUIMachine {


//Dumb Cursed Tests



    @Override
    public void onLoad() {
        super.onLoad();
    }


    @Nullable @Getter
    public FluidTransferList inputFluidInventory;

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new BasicSteamTurbineLogic(this);
    }


    public BasicSteamTurbineMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    //Forming Logic
    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        getRecipeLogic().resetRecipeLogic();
        // capture all energy containers
        List<IFluidTransfer> fluidTanks = new ArrayList<>();
        Map<Long, IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts()) {
            IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.BOTH);
            if(io == IO.NONE || io == IO.IN) continue;
            for (var handler : part.getRecipeHandlers()) {
                if (io != IO.BOTH && handler.getHandlerIO() != IO.BOTH && io != handler.getHandlerIO()) continue;
                var handlerIO = io == IO.OUT ? handler.getHandlerIO() : io;
                if(handler.getCapability() == StressRecipeCapability.CAP && handler instanceof IKineticMachine kineticMachine) {
                    //SpinnyBoi.add(kineticMachine);
                } else if (handlerIO == IO.IN && handler.getCapability() == FluidRecipeCapability.CAP && handler instanceof IFluidTransfer fluidTransfer) {
                    fluidTanks.add(fluidTransfer);
                }
            }
        }
        this.inputFluidInventory = new FluidTransferList(fluidTanks);
    }

    //Spaghetti Test


    //////////////////////////////////////
    //******     Recipe Logic    *******//
    //////////////////////////////////////


//NOT USED
    @Nullable
    public static GTRecipe recipeModifier(MetaMachine machine, @Nonnull GTRecipe recipe) {
        if (machine instanceof BasicSteamTurbineMachine steamturbineMachine) {
                recipe.getTickOutputContents(StressRecipeCapability.CAP);
                return recipe; }
                if(recipe.getTickInputContents(FluidRecipeCapability.CAP).isEmpty()) {
                    return null;
            }
        return recipe;
   }

    @Override
    public boolean alwaysTryModifyRecipe() {
        return false;
    }

    @Override
    public boolean dampingWhenWaiting() {
        return true;
    }


    @Override
    public void addDisplayText(List<Component> textList) {
        if (isFormed()) {
            var maxVoltage = getMaxVoltage();
            if (maxVoltage > 0) {
                String voltageName = GTValues.VNF[GTUtil.getFloorTierByVoltage(maxVoltage)];
                textList.add(Component.translatable("gtceu.multiblock.max_energy_per_tick", maxVoltage, voltageName));
            }
        }
    }
}