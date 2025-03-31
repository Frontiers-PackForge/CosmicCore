package com.ghostipedia.cosmiccore.client.renderer.item;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.GTCEu;

import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.lowdragmc.lowdraglib.utils.ColorUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import org.joml.Matrix4f;

import java.util.function.Supplier;

public class HaloRenders implements IRenderer {

    public static final HaloRenders PRISMATIC_TUNGSTEN_HALO = HaloRenders.create(0.15F, 0xeb34cf, 10,
            CosmicCore.id("rnd/halo"));

    private static HaloRenders create(float pulse, int colour, int size, ResourceLocation textures) {
        return create(pulse, () -> colour, () -> size, textures);
    }

    private static HaloRenders create(float pulse, Supplier<Integer> colour, Supplier<Integer> size,
                                      ResourceLocation textures) {
        return GTCEu.isClientSide() ? new HaloRenders(pulse, colour, size, textures) : null;
    }

    private final float pulse;
    private final Supplier<Integer> colour;
    private final Supplier<Integer> size;
    private final ResourceLocation texture;

    private HaloRenders(float pulse, Supplier<Integer> colour, Supplier<Integer> size, ResourceLocation texture) {
        this.pulse = pulse;
        this.colour = colour;
        this.size = size;
        this.texture = texture;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, boolean leftHand, PoseStack poseStack,
                           MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model) {
        model = getVanillaModel(stack, null, null);
        if (transformType == ItemDisplayContext.GUI) {
            if (texture != null) {
                poseStack.pushPose();
                poseStack.translate(-0.5F, -0.5F, -0.5F);
                Tesselator tess = Tesselator.getInstance();
                BufferBuilder buf = tess.getBuilder();
                buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                RenderSystem.enableBlend();
                RenderSystem.disableDepthTest();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                int colour = this.colour.get();
                RenderSystem.setShaderColor(ColorUtils.red(colour), ColorUtils.green(colour), ColorUtils.blue(colour),
                        ColorUtils.alpha(colour));
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
                TextureAtlasSprite sprite = getBlockSprite(texture);
                float minU = sprite.getU0();
                float maxU = sprite.getU1();
                float minV = sprite.getV0();
                float maxV = sprite.getV1();
                float spread = size.get() / 16.0F;
                float min = 0.0F - spread;
                float max = 1.0F + spread;
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
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        } else {
            vanillaRender(stack, transformType, leftHand, poseStack, buffer, combinedLight, combinedOverlay, model);
        }
    }

    public static void vanillaRender(ItemStack stack, ItemDisplayContext transformType, boolean leftHand,
                                     PoseStack poseStack, MultiBufferSource buffer, int combinedLight,
                                     int combinedOverlay, BakedModel model) {
        IItemRendererProvider.disabled.set(true);
        Minecraft.getInstance().getItemRenderer().render(stack, transformType, leftHand, poseStack, buffer,
                combinedLight, combinedOverlay, getVanillaModel(stack, null, null));
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

    public static TextureAtlasSprite getBlockSprite(ResourceLocation location) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(location);
    }
}
