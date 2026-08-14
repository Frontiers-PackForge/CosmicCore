package com.ghostipedia.cosmiccore.client.renderer.item;

import com.ghostipedia.cosmiccore.api.item.component.ICustomRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;

@OnlyIn(Dist.CLIENT)
public interface CosmicItemRenderer {

    void renderItem(ICustomRenderer renderData, ItemStack stack, ItemDisplayContext transformType, boolean leftHand,
                    PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay,
                    BakedModel model);

    default boolean useBlockLight(ICustomRenderer renderData, ItemStack stack) {
        return false;
    }
}
