package com.ghostipedia.cosmiccore.client;

import com.ghostipedia.cosmiccore.common.food.hearth.HearthPlateBlockEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

public class HearthPlateRenderer implements BlockEntityRenderer<HearthPlateBlockEntity> {

    public HearthPlateRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(HearthPlateBlockEntity plate, float partialTick, PoseStack pose, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        renderFlat(renderer, plate, plate.main, pose, buffer, packedLight, packedOverlay, 0.5f, 0.5f, 0.55f);
        renderFlat(renderer, plate, plate.side, pose, buffer, packedLight, packedOverlay, 0.24f, 0.76f, 0.3f);
        renderUpright(renderer, plate, plate.drink, pose, buffer, packedLight, packedOverlay, 0.8f, 0.24f, 0.35f);
    }

    private static void renderFlat(ItemRenderer renderer, HearthPlateBlockEntity plate, ItemStack stack,
                                   PoseStack pose, MultiBufferSource buffer, int light, int overlay,
                                   float x, float z, float scale) {
        if (stack.isEmpty()) return;
        pose.pushPose();
        pose.translate(x, 0.15, z);
        pose.mulPose(Axis.XP.rotationDegrees(90));
        pose.scale(scale, scale, scale);
        renderer.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, pose, buffer, plate.getLevel(), 0);
        pose.popPose();
    }

    private static void renderUpright(ItemRenderer renderer, HearthPlateBlockEntity plate, ItemStack stack,
                                      PoseStack pose, MultiBufferSource buffer, int light, int overlay,
                                      float x, float z, float scale) {
        if (stack.isEmpty()) return;
        pose.pushPose();
        pose.translate(x, 0.33, z);
        pose.scale(scale, scale, scale);
        renderer.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, pose, buffer, plate.getLevel(), 0);
        pose.popPose();
    }
}
