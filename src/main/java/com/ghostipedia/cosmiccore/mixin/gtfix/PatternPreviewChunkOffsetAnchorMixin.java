package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.client.renderer.PreviewMatrixHolder;

import com.gregtechceu.gtceu.client.renderer.PatternPreviewRenderer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PatternPreviewRenderer.class, remap = false)
public abstract class PatternPreviewChunkOffsetAnchorMixin {

    @Shadow
    private BlockPos controllerPos;

    @Redirect(method = "renderBlocks",
              at = @At(value = "INVOKE",
                       target = "Lcom/mojang/blaze3d/vertex/PoseStack$Pose;pose()Lorg/joml/Matrix4f;"),
              require = 1)
    private Matrix4f cosmiccore$anchorViaModelView(PoseStack.Pose pose, @Local(argsOnly = true) Vec3 cameraPos) {
        return new Matrix4f(PreviewMatrixHolder.FRUSTUM)
                .translate((float) (controllerPos.getX() - cameraPos.x),
                        (float) (controllerPos.getY() - cameraPos.y),
                        (float) (controllerPos.getZ() - cameraPos.z));
    }

    @Inject(method = "renderBlocks",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/ShaderInstance;apply()V",
                     shift = At.Shift.AFTER),
            require = 1)
    private void cosmiccore$zeroChunkOffset(RenderType renderType, PoseStack poseStack, Vec3 cameraPos,
                                            CallbackInfo ci) {
        ShaderInstance shader = RenderSystem.getShader();
        if (shader == null) return;
        Uniform chunkOffset = shader.CHUNK_OFFSET;
        if (chunkOffset != null) {
            chunkOffset.set(0.0F, 0.0F, 0.0F);
            chunkOffset.upload();
        }
    }
}
