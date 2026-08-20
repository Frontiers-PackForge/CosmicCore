package com.ghostipedia.cosmiccore.mixin.ebfix;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.world.level.block.state.BlockState;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import neoforge.nl.requios.effortlessbuilding.render.BlockPreviewRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockPreviewRenderer.class)
public abstract class BlockPreviewRendererIsolationMixin {

    @WrapOperation(
                   method = "render",
                   at = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderSingleBlock(Lnet/minecraft/world/level/block/state/BlockState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"))
    private static void cosmiccore$isolateBrokenBlockPreview(
                                                             BlockRenderDispatcher renderer, BlockState state,
                                                             PoseStack poseStack, MultiBufferSource buffers, int light,
                                                             int overlay, Operation<Void> original) {
        try {
            original.call(renderer, state, poseStack, buffers, light, overlay);
        } catch (RuntimeException ignored) {}
    }
}
