package com.ghostipedia.cosmiccore.client.renderer.item;

import com.ghostipedia.cosmiccore.client.gui.AlphaOverrideVertexConsumer;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;

import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.lowdragmc.lowdraglib.utils.ColorUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import org.joml.Matrix4f;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.ghostipedia.cosmiccore.client.renderer.utility.CosmicCoreRenderUtils.bindBlockAtlas;

public class HaloItemRenderer extends WrappedItemRenderer implements IRenderer {

    private final Set<ResourceLocation> textures = new HashSet<>();

    int haloSize;
    Supplier<Integer> haloColor;
    ResourceLocation haloTexture;
    boolean shouldDrawHalo;
    boolean shouldDrawPulse;

    public static HaloItemRenderer create(int size, int color, ResourceLocation texture, boolean drawHalo,
                                          boolean drawPulse) {
        return create(size, () -> color, texture, drawHalo, drawPulse);
    }

    public static HaloItemRenderer create(int size, Supplier<Integer> color, ResourceLocation texture, boolean drawHalo,
                                          boolean drawPulse) {
        return GTCEu.isClientSide() ? new HaloItemRenderer(size, color, texture, drawHalo, drawPulse) : null;
    }

    private HaloItemRenderer(int size, Supplier<Integer> color, ResourceLocation texture, boolean drawHalo,
                             boolean drawPulse) {
        this.haloSize = size;
        this.haloColor = color;
        this.haloTexture = texture;
        this.shouldDrawHalo = drawHalo;
        this.shouldDrawPulse = drawPulse;

        if (Platform.isClient()) {
            addTexture(haloTexture);
            registerEvent();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void addTexture(ResourceLocation resourceLocation) {
        textures.add(resourceLocation);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, boolean leftHand, PoseStack poseStack,
                           MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model) {
        model = getVanillaModel(stack, null, null);
        if (transformType == ItemDisplayContext.GUI) {
            Tesselator tess = Tesselator.getInstance();
            BufferBuilder buf = tess.getBuilder();
            buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

            poseStack.pushPose();
            RenderSystem.enableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            poseStack.translate(-0.5F, -0.5F, -0.5F);

            if (shouldDrawHalo) {
                int colour = haloColor.get();
                float r = ColorUtils.red(colour);
                float g = ColorUtils.green(colour);
                float b = ColorUtils.blue(colour);
                float a = ColorUtils.alpha(colour);

                RenderSystem.setShaderColor(r, g, b, a);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                TextureAtlasSprite sprite = ModelFactory.getBlockSprite(haloTexture);
                bindBlockAtlas();
                float spread = haloSize / 16F;
                float min = 0F - spread;
                float max = 1F + spread;

                float minU = sprite.getU0();
                float maxU = sprite.getU1();
                float minV = sprite.getV0();
                float maxV = sprite.getV1();

                Matrix4f pos = poseStack.last().pose();
                buf.vertex(pos, max, max, 0).uv(maxU, minV).endVertex();
                buf.vertex(pos, min, max, 0).uv(minU, minV).endVertex();
                buf.vertex(pos, min, min, 0).uv(minU, maxV).endVertex();
                buf.vertex(pos, max, min, 0).uv(maxU, maxV).endVertex();
                tess.end();

                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                poseStack.popPose();
            }
            vanillaRender(stack, transformType, leftHand, poseStack, buffer, combinedLight, combinedOverlay, model);

            if (shouldDrawPulse) {
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

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        if (atlasName.equals(InventoryMenu.BLOCK_ATLAS)) {
            textures.forEach(register);
        }
    }
}
