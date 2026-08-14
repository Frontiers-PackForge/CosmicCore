package com.ghostipedia.cosmiccore.client.renderer.item;

import com.ghostipedia.cosmiccore.api.item.component.ICustomRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;

public class RadianceItemRenderer extends WrappedItemRenderer {

    public static final RadianceItemRenderer INSTANCE = new RadianceItemRenderer();

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderItem(ICustomRenderer renderData, ItemStack stack, ItemDisplayContext transformType,
                           boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int combinedLight,
                           int combinedOverlay, BakedModel model) {
        poseStack.pushPose();
        if (transformType == ItemDisplayContext.GUI) {
            poseStack.scale(1.4F, 1.4F, 1F);
            poseStack.mulPose(
                    new Quaternionf().fromAxisAngleDeg(0.0f, 0.0f, 0.3f, (System.currentTimeMillis() / 25) % 360));
        }

        vanillaRender(stack, transformType, leftHand, poseStack, buffer, combinedLight, combinedOverlay, model);
        poseStack.popPose();
    }
}
