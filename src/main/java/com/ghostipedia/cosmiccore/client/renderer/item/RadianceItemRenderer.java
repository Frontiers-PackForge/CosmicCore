package com.ghostipedia.cosmiccore.client.renderer.item;

import com.gregtechceu.gtceu.api.GTValues;
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
import org.embeddedt.modernfix.render.RenderState;
import org.joml.Quaternionf;

public class RadianceItemRenderer implements IRenderer {


    public static final RadianceItemRenderer INSTANCE = new RadianceItemRenderer();
    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model) {

        poseStack.pushPose();
        if (transformType == ItemDisplayContext.GUI) {
            float scalefactor = GTValues.RNG.nextFloat() * 0.2F + 0.95F;
            poseStack.scale(scalefactor, scalefactor, 1F);
        }

        RenderState.IS_RENDERING_LEVEL = true;
        vanillaRender(stack, transformType, leftHand, poseStack, buffer, combinedLight, combinedOverlay, model);
        RenderState.IS_RENDERING_LEVEL = false;
        poseStack.popPose();

    }
    public static void vanillaRender(ItemStack stack, ItemDisplayContext transformType, boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model) {
        IItemRendererProvider.disabled.set(true);
        Minecraft.getInstance().getItemRenderer().render(stack, transformType, leftHand, poseStack, buffer, combinedLight, combinedOverlay, getVanillaModel(stack, null, null));
        IItemRendererProvider.disabled.set(false);
    }


    public static ItemRenderer getItemRenderer() {
        return Minecraft.getInstance().getItemRenderer();
    }

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
//TODO ; I hate math, also make this a helper class rather than dumping all the same functions into here every time!
//Avarita Pulse Effect? float scalefactor = (float)(Math.sin((Minecraft.getInstance().getDeltaFrameTime() % 360) / 5.F * 180 / Math.PI) + 1)/2.F;
//Rotate poseStack.mulPose(new Quaternionf().fromAxisAngleDeg(0f, 0.75f, 0.12f, (System.currentTimeMillis() / 15) % 360));