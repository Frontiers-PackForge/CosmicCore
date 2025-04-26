package com.ghostipedia.cosmiccore.client.renderer.item;

import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class WrappedItemRenderer implements IRenderer {

    @OnlyIn(Dist.CLIENT)
    public static void vanillaRender(ItemStack stack, ItemDisplayContext transformType, boolean leftHand,
                                     PoseStack poseStack, MultiBufferSource buffer, int combinedLight,
                                     int combinedOverlay, BakedModel model) {
        IItemRendererProvider.disabled.set(true);
        Minecraft.getInstance().getItemRenderer().render(stack, transformType, leftHand, poseStack, buffer,
                combinedLight, combinedOverlay, model);
        IItemRendererProvider.disabled.set(false);
    }

    public static ItemRenderer getItemRenderer() {
        return Minecraft.getInstance().getItemRenderer();
    }
    @OnlyIn(Dist.CLIENT)
    public static BakedModel getVanillaModel(ItemStack stack, ClientLevel level, LivingEntity entity) {
        ItemModelShaper shaper = getItemRenderer().getItemModelShaper();
        BakedModel model = shaper.getItemModel(stack.getItem());
        if (model != null) {
            BakedModel bakedmodel = model.getOverrides().resolve(model, stack, level, entity, 0);
            if (bakedmodel != null) return bakedmodel;
        }
        return shaper.getModelManager().getMissingModel();
    }
}
