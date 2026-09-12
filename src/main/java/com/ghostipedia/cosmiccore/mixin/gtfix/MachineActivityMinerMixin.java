package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.api.machine.activity.ActivityScope;
import com.ghostipedia.cosmiccore.api.machine.activity.MachineActivity;
import com.ghostipedia.cosmiccore.common.machine.trait.activity.MachineActivityRuntime;

import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.common.machine.trait.miner.MinerLogic;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(value = MinerLogic.class, remap = false)
public abstract class MachineActivityMinerMixin {

    @WrapMethod(method = "serverTick")
    private void cosmiccore$tick(Operation<Void> original) {
        try (ActivityScope ignored = MachineActivityRuntime.scope((RecipeLogic) (Object) this)) {
            original.call();
        }
    }

    @WrapMethod(method = "doPostProcessing")
    private boolean cosmiccore$virtualDrops(NonNullList<ItemStack> drops, BlockState state, LootParams.Builder builder,
                                            Operation<Boolean> original) {
        try (ActivityScope ignored = ActivityScope.suspend()) {
            return original.call(drops, state, builder);
        }
    }

    @WrapOperation(method = "mineAndInsertItems",
                   at = @At(value = "INVOKE",
                            target = "Lcom/gregtechceu/gtceu/utils/GTTransferUtils;addItemsToItemHandler(Lnet/neoforged/neoforge/items/IItemHandlerModifiable;ZLjava/util/List;)Z"))
    private boolean cosmiccore$mine(IItemHandlerModifiable handler, boolean simulated, List<ItemStack> drops,
                                    Operation<Boolean> original) {
        if (simulated) return original.call(handler, true, drops);
        MinerLogic logic = (MinerLogic) (Object) this;
        MachineActivity activity = MachineActivityRuntime.activity(logic.getMachine());
        if (activity != null) activity.begin(0, "gtceu:mining", Math.max(1, logic.getSpeed()));
        boolean accepted = original.call(handler, false, drops);
        if (activity != null) {
            if (accepted) activity.complete(0);
            else {
                activity.failure(0, "output");
                activity.interrupt(0);
            }
        }
        return accepted;
    }
}
