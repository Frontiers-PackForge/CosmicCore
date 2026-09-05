package com.ghostipedia.cosmiccore.mixin.sodium.deployment;

import com.ghostipedia.cosmiccore.client.renderer.deployment.LeylineDeploymentClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.RenderSection", remap = false)
public abstract class LeylineSodiumSectionUploadMixin {

    @Shadow
    public abstract int getChunkX();

    @Shadow
    public abstract int getChunkY();

    @Shadow
    public abstract int getChunkZ();

    @Inject(method = "setLastUploadFrame", at = @At("TAIL"))
    private void cosmiccore$deploymentTerrainReady(int frame, CallbackInfo ci) {
        LeylineDeploymentClient.sectionUploaded(getChunkX(), getChunkY(), getChunkZ());
    }
}
