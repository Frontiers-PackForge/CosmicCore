package com.ghostipedia.cosmiccore.mixin.aether;

import com.ghostipedia.cosmiccore.client.aether.AetherAtmosphereRenderer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.aetherteam.aether.client.renderer.level.AetherSkyRenderEffects", remap = false)
public abstract class AetherAtmosphereMixin {

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$renderAtmosphere(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix,
                                             Camera camera, Matrix4f projectionMatrix, boolean isFoggy,
                                             Runnable setupFog, CallbackInfoReturnable<Boolean> cir) {
        if (AetherAtmosphereRenderer.render(
                level, ticks, partialTick, modelViewMatrix, camera, projectionMatrix, isFoggy, setupFog)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getBrightnessDependentFogColor", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$replaceFogColor(Vec3 color, float brightness, CallbackInfoReturnable<Vec3> cir) {
        cir.setReturnValue(AetherAtmosphereRenderer.fogColor(brightness));
    }

    @Inject(method = "getCloudColor", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$replaceCloudColor(ClientLevel level, float partialTick, CallbackInfoReturnable<Vec3> cir) {
        cir.setReturnValue(AetherAtmosphereRenderer.cloudColor(level, partialTick));
    }
}
