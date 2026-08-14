package com.ghostipedia.cosmiccore.mixin.client;

import com.ghostipedia.cosmiccore.api.item.component.ICustomRenderer;
import com.ghostipedia.cosmiccore.api.item.component.ICustomRendererProvider;
import com.ghostipedia.cosmiccore.client.renderer.item.CosmicItemRenderer;
import com.ghostipedia.cosmiccore.client.renderer.item.CosmicItemRenderers;
import com.ghostipedia.cosmiccore.client.renderer.item.WrappedItemRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class CosmicItemRendererMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void cosmicCore$render(ItemStack stack, ItemDisplayContext transformType, boolean leftHand,
                                   PoseStack poseStack, MultiBufferSource buffer, int combinedLight,
                                   int combinedOverlay, BakedModel model, CallbackInfo ci) {
        if (WrappedItemRenderer.isVanillaRendering() ||
                !(stack.getItem() instanceof ICustomRendererProvider provider)) {
            return;
        }
        ICustomRenderer renderData = provider.getRenderInfo(stack);
        if (renderData == null) {
            return;
        }
        CosmicItemRenderer renderer = CosmicItemRenderers.resolve(renderData);
        if (renderer == null) {
            return;
        }

        boolean gui = transformType == ItemDisplayContext.GUI;
        boolean useBlockLight = renderer.useBlockLight(renderData, stack);
        if (gui && useBlockLight != model.usesBlockLight()) {
            if (useBlockLight) {
                Lighting.setupFor3DItems();
            } else {
                Lighting.setupForFlatItems();
            }
        }
        try {
            renderer.renderItem(renderData, stack, transformType, leftHand, poseStack, buffer, combinedLight,
                    combinedOverlay, model);
            if (gui && buffer instanceof MultiBufferSource.BufferSource bufferSource) {
                bufferSource.endBatch();
            }
        } finally {
            if (gui) {
                if (model.usesBlockLight()) {
                    Lighting.setupFor3DItems();
                } else {
                    Lighting.setupForFlatItems();
                }
            }
        }
        ci.cancel();
    }
}
