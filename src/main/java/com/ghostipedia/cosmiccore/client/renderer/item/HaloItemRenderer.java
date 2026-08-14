package com.ghostipedia.cosmiccore.client.renderer.item;

import com.ghostipedia.cosmiccore.api.item.component.HaloItemRenderData;
import com.ghostipedia.cosmiccore.api.item.component.ICustomRenderer;
import com.ghostipedia.cosmiccore.client.gui.AlphaOverrideVertexConsumer;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.FastColor;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import org.joml.Matrix4f;

import static com.ghostipedia.cosmiccore.client.renderer.utility.CosmicCoreRenderUtils.bindBlockAtlas;

public class HaloItemRenderer extends WrappedItemRenderer {

    public static final HaloItemRenderer INSTANCE = new HaloItemRenderer();

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderItem(ICustomRenderer renderData, ItemStack stack, ItemDisplayContext transformType,
                           boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int combinedLight,
                           int combinedOverlay, BakedModel model) {
        if (!(renderData instanceof HaloItemRenderData halo)) {
            return;
        }
        model = getVanillaModel(stack, null, null);
        if (transformType == ItemDisplayContext.GUI) {
            RenderSystem.enableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            if (halo.drawHalo()) {
                BufferBuilder buf = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
                        DefaultVertexFormat.POSITION_TEX);
                poseStack.pushPose();
                poseStack.translate(-0.5F, -0.5F, -0.5F);
                int colour = halo.color().getAsInt();
                float r = FastColor.ARGB32.red(colour) / 255.0F;
                float g = FastColor.ARGB32.green(colour) / 255.0F;
                float b = FastColor.ARGB32.blue(colour) / 255.0F;
                float a = FastColor.ARGB32.alpha(colour) / 255.0F;

                RenderSystem.setShaderColor(r, g, b, a);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                        .apply(halo.texture());
                bindBlockAtlas();
                float spread = halo.size() / 16F;
                float min = 0F - spread;
                float max = 1F + spread;

                float minU = sprite.getU0();
                float maxU = sprite.getU1();
                float minV = sprite.getV0();
                float maxV = sprite.getV1();

                Matrix4f pos = poseStack.last().pose();
                buf.addVertex(pos, max, max, 0).setUv(maxU, minV);
                buf.addVertex(pos, min, max, 0).setUv(minU, minV);
                buf.addVertex(pos, min, min, 0).setUv(minU, maxV);
                buf.addVertex(pos, max, min, 0).setUv(maxU, maxV);
                BufferUploader.drawWithShader(buf.buildOrThrow());

                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                poseStack.popPose();
            }
            vanillaRender(stack, transformType, leftHand, poseStack, buffer, combinedLight, combinedOverlay, model);

            if (halo.drawPulse()) {
                poseStack.pushPose();
                poseStack.translate(-0.5F, -0.5F, -0.5F);
                float scale = GTValues.RNG.nextFloat() * 0.15F + 0.95F;
                double trans = (1 - scale) / 2;
                poseStack.translate(trans, trans, 0);
                poseStack.scale(scale, scale, 1.0001F);

                renderAlpha(stack, transformType, leftHand, poseStack, buffer, combinedLight, combinedOverlay,
                        model, 0.6F);

                poseStack.popPose();
            }
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        } else {
            vanillaRender(stack, transformType, leftHand, poseStack, buffer, combinedLight, combinedOverlay, model);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void renderAlpha(ItemStack stack, ItemDisplayContext modelTransformationMode, boolean leftHanded,
                                   PoseStack matrices, MultiBufferSource buffer, int light, int overlay,
                                   BakedModel model, float alphaOverride) {
        if (stack.isEmpty()) return;
        model.getTransforms().getTransform(modelTransformationMode).apply(leftHanded, matrices);
        RenderType renderType = ItemBlockRenderTypes.getRenderType(stack, true);
        VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(buffer, renderType, true, stack.hasFoil());

        Minecraft.getInstance().getItemRenderer()
                .renderModelLists(model, stack, light, overlay, matrices,
                        new AlphaOverrideVertexConsumer(vertexConsumer, alphaOverride));
    }
}
