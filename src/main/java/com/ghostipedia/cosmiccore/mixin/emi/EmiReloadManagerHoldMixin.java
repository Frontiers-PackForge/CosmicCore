package com.ghostipedia.cosmiccore.mixin.emi;

import com.ghostipedia.cosmiccore.integration.emi.warmer.EmiSizeWarmer;

import dev.emi.emi.runtime.EmiReloadManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EmiReloadManager.class, remap = false)
public abstract class EmiReloadManagerHoldMixin {

    @Inject(method = "isLoaded", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cosmiccore$holdUntilWarm(CallbackInfoReturnable<Boolean> cir) {
        if (EmiSizeWarmer.isHoldingEmi()) {
            cir.setReturnValue(false);
        }
    }
}
