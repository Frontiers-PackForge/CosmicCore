package com.ghostipedia.cosmiccore.client.renderer.item;

import com.ghostipedia.cosmiccore.api.item.component.ICustomRenderer;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;

public class LensRender extends WrappedItemRenderer {

    public static final LensRender INSTANCE = new LensRender();

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderItem(ICustomRenderer renderData, ItemStack stack, ItemDisplayContext transformType,
                           boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int combinedLight,
                           int combinedOverlay, BakedModel model) {
        poseStack.pushPose();
        if (transformType == ItemDisplayContext.GUI) {
            float scalefactor = GTValues.RNG.nextFloat() * 0.2F + 0.95F;
            poseStack.scale(scalefactor, scalefactor, 1F);
        }

        vanillaRender(stack, transformType, leftHand, poseStack, buffer, combinedLight, combinedOverlay, model);
        poseStack.popPose();
    }
}
// TODO ; I hate math, also make this a helper class rather than dumping all the same functions into here every time!
// Avarita Pulse Effect? float scalefactor = (float)(Math.sin((Minecraft.getInstance().getDeltaFrameTime() % 360) / 5.F
// * 180 / Math.PI) + 1)/2.F;
// Rotate poseStack.mulPose(new Quaternionf().fromAxisAngleDeg(0f, 0.75f, 0.12f, (System.currentTimeMillis() / 15) %
// 360));
