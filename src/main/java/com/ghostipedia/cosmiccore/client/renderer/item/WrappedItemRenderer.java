package com.ghostipedia.cosmiccore.client.renderer.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;

public abstract class WrappedItemRenderer implements CosmicItemRenderer {

    private static final ThreadLocal<Integer> VANILLA_RENDER_DEPTH = ThreadLocal.withInitial(() -> 0);

    public static boolean isVanillaRendering() {
        return VANILLA_RENDER_DEPTH.get() > 0;
    }

    @OnlyIn(Dist.CLIENT)
    public static void vanillaRender(ItemStack stack, ItemDisplayContext transformType, boolean leftHand,
                                     PoseStack poseStack, MultiBufferSource buffer, int combinedLight,
                                     int combinedOverlay, BakedModel model) {
        int depth = VANILLA_RENDER_DEPTH.get();
        VANILLA_RENDER_DEPTH.set(depth + 1);
        try {
            Minecraft.getInstance().getItemRenderer().render(stack, transformType, leftHand, poseStack, buffer,
                    combinedLight, combinedOverlay, model);
        } finally {
            if (depth == 0) {
                VANILLA_RENDER_DEPTH.remove();
            } else {
                VANILLA_RENDER_DEPTH.set(depth);
            }
        }
    }

    public static ItemRenderer getItemRenderer() {
        return Minecraft.getInstance().getItemRenderer();
    }

    @OnlyIn(Dist.CLIENT)
    public static BakedModel getVanillaModel(ItemStack stack, ClientLevel level, LivingEntity entity) {
        return getItemRenderer().getModel(stack, level, entity, 0);
    }
}
