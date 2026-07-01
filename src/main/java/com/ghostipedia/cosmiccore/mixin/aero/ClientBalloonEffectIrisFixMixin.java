package com.ghostipedia.cosmiccore.mixin.aero;

import com.ghostipedia.cosmiccore.client.compat.IrisCompat;

import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.effect.ClientBalloonEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientBalloonEffectRenderer.class, remap = false)
public class ClientBalloonEffectIrisFixMixin {

    @Inject(method = "renderBalloonEffects", at = @At("HEAD"), cancellable = true)
    private static void cosmiccore$suppressUnderShaders(CallbackInfo ci) {
        if (IrisCompat.shadersActive()) {
            ci.cancel();
        }
    }
}
