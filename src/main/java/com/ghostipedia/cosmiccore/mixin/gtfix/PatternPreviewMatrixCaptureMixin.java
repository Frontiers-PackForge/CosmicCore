package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.client.renderer.PreviewMatrixHolder;

import com.gregtechceu.gtceu.client.ClientEventListener;

import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientEventListener.class, remap = false)
public class PatternPreviewMatrixCaptureMixin {

    @Inject(method = "onRenderLevelStageEvent", at = @At("HEAD"))
    private static void cosmiccore$capturePreviewMatrices(RenderLevelStageEvent event, CallbackInfo ci) {
        PreviewMatrixHolder.update(event.getModelViewMatrix(), event.getProjectionMatrix());
    }
}
