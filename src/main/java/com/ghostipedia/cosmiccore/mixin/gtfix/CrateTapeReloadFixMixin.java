package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.storage.CrateMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MetaMachine.class, remap = false)
public abstract class CrateTapeReloadFixMixin {

    @Inject(method = "onLoad", at = @At("TAIL"), remap = false)
    private void cosmiccore$reapplyCrateTapeDropState(CallbackInfo ci) {
        if ((Object) this instanceof CrateMachine crate) {
            crate.inventory.shouldDropInventoryInWorld(!crate.isTaped());
        }
    }
}
