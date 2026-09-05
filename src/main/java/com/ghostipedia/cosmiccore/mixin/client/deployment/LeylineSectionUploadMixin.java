package com.ghostipedia.cosmiccore.mixin.client.deployment;

import com.ghostipedia.cosmiccore.client.renderer.deployment.LeylineDeploymentClient;

import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SectionRenderDispatcher.RenderSection.class)
public abstract class LeylineSectionUploadMixin {

    @Shadow
    public abstract BlockPos getOrigin();

    @Inject(method = "setCompiled", at = @At("TAIL"))
    private void cosmiccore$deploymentTerrainReady(SectionRenderDispatcher.CompiledSection compiled, CallbackInfo ci) {
        BlockPos origin = getOrigin();
        LeylineDeploymentClient.sectionUploaded(SectionPos.blockToSectionCoord(origin.getX()),
                SectionPos.blockToSectionCoord(origin.getY()), SectionPos.blockToSectionCoord(origin.getZ()));
    }
}
