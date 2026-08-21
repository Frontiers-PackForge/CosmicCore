package com.ghostipedia.cosmiccore.mixin.gttweak.power;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IRecipeTierBoostMachine;
import com.ghostipedia.cosmiccore.api.machine.multiblock.RecipeTierBoostState;
import com.ghostipedia.cosmiccore.common.power.MultiblockRecipeTierBoost;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WorkableElectricMultiblockMachine.class, remap = false)
public abstract class MultiblockRecipeTierBoostMixin implements IRecipeTierBoostMachine {

    @Unique
    private boolean cosmiccore$supportsRecipeTierBoost;

    @Inject(method = "formStructure", at = @At("HEAD"))
    private void cosmiccore$resolveRecipeTierBoostPolicy(String substructureName, CallbackInfo ci) {
        cosmiccore$supportsRecipeTierBoost = MultiblockRecipeTierBoost.supportsRecipeTierBoost(
                (WorkableElectricMultiblockMachine) (Object) this);
    }

    @Inject(method = "getMaxVoltage", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$applyRecipeTierBoost(CallbackInfoReturnable<Long> cir) {
        WorkableElectricMultiblockMachine machine = (WorkableElectricMultiblockMachine) (Object) this;
        if (machine.isGenerator()) return;
        cir.setReturnValue(getRecipeTierBoostState().maximumRecipeVoltage());
    }

    @Override
    public boolean supportsRecipeTierBoost() {
        return cosmiccore$supportsRecipeTierBoost;
    }

    @Override
    public RecipeTierBoostState getRecipeTierBoostState() {
        return MultiblockRecipeTierBoost.evaluate(
                (WorkableElectricMultiblockMachine) (Object) this,
                cosmiccore$supportsRecipeTierBoost);
    }
}
