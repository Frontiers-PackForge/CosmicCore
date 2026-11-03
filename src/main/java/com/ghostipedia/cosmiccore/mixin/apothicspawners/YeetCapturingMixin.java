package com.ghostipedia.cosmiccore.mixin.apothicspawners;

import dev.shadowsoffire.apothic_spawners.ASEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ASEvents.class)
public abstract class YeetCapturingMixin {

    @Inject(method = "handleCapturing", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$disableCapturing(CallbackInfo ci) {
        ci.cancel();
    }
}
