package com.ghostipedia.cosmiccore.client.map.xaero.worldmap;

import com.ghostipedia.cosmiccore.client.map.RevealedField;
import com.ghostipedia.cosmiccore.client.map.RevealedFields;
import com.ghostipedia.cosmiccore.client.map.xaero.FieldBlobDraw;
import com.ghostipedia.cosmiccore.client.map.xaero.FieldBlobElement;

import com.gregtechceu.gtceu.integration.map.GroupingMapRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureManager;

import xaero.map.element.MapElementReader;
import xaero.map.element.MapElementRenderProvider;
import xaero.map.element.MapElementRenderer;
import xaero.map.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;

public class FieldBlobElementRenderer
                                      extends MapElementRenderer<FieldBlobElement, Object, FieldBlobElementRenderer> {

    protected FieldBlobElementRenderer(Object context,
                                       MapElementRenderProvider<FieldBlobElement, Object> provider,
                                       MapElementReader<FieldBlobElement, Object, FieldBlobElementRenderer> reader) {
        super(context, provider, reader);
    }

    @Override
    public boolean shouldBeDimScaled() {
        return false;
    }

    @Override
    public void beforeRender(int location, Minecraft mc, GuiGraphics guiGraphics,
                             double cameraX, double cameraZ, double mouseX, double mouseZ,
                             float brightness, double scale, double screenSizeBasedScale, TextureManager textureManager,
                             Font fontRenderer,
                             MultiBufferSource.BufferSource renderTypeBuffers,
                             MultiTextureRenderTypeRendererProvider rendererProvider,
                             boolean pre) {
        if (!pre && GroupingMapRenderer.getInstance().doShowLayer(FieldBlobDraw.ORE_VEINS_LAYER)) {
            FieldBlobDraw.beginZoneBatch();
        }
    }

    @Override
    public void afterRender(int location, Minecraft mc, GuiGraphics guiGraphics,
                            double cameraX, double cameraZ, double mouseX, double mouseZ,
                            float brightness, double scale, double screenSizeBasedScale,
                            TextureManager textureManager, Font fontRenderer,
                            MultiBufferSource.BufferSource renderTypeBuffers,
                            MultiTextureRenderTypeRendererProvider rendererProvider,
                            boolean pre) {
        if (!pre && GroupingMapRenderer.getInstance().doShowLayer(FieldBlobDraw.ORE_VEINS_LAYER)) {
            FieldBlobDraw.endZoneBatch();
        }
    }

    @Override
    public void renderElementPre(int location, FieldBlobElement w, boolean hovered,
                                 Minecraft mc, GuiGraphics guiGraphics,
                                 double cameraX, double cameraZ, double mouseX, double mouseZ,
                                 float brightness, double scale, double screenSizeBasedScale,
                                 TextureManager textureManager, Font fontRenderer,
                                 MultiBufferSource.BufferSource renderTypeBuffers,
                                 MultiTextureRenderTypeRendererProvider rendererProvider,
                                 float optionalScale, double partialX, double partialY,
                                 boolean cave,
                                 float partialTicks) {}

    @Override
    public boolean renderElement(int location, FieldBlobElement element,
                                 boolean hovered,
                                 Minecraft mc, GuiGraphics graphics,
                                 double cameraX, double cameraZ, double mouseX, double mouseZ,
                                 float brightness, double scale, double screenSizeBasedScale,
                                 TextureManager textureManager, Font fontRenderer,
                                 MultiBufferSource.BufferSource renderTypeBuffers,
                                 MultiTextureRenderTypeRendererProvider rendererProvider,
                                 int elementIndex, double optionalDepth, float optionalScale,
                                 double partialX, double partialY,
                                 boolean cave, float partialTicks) {
        RevealedField field = element.field();
        float pixelRadius = FieldBlobDraw.zonePixelRadius(FieldBlobDraw.zoneBlockRadius(field.tier(), field.radius()),
                scale);
        boolean depleted = RevealedFields.INSTANCE.isDepleted(element.dimension(), field.x(), field.z());
        FieldBlobDraw.addZone(graphics.pose().last().pose(), pixelRadius, field.colorRGB(),
                FieldBlobDraw.shapeSeed(field.x(), field.z()), depleted);
        return true;
    }

    @Override
    public boolean shouldRender(int location, boolean pre) {
        return !pre && GroupingMapRenderer.getInstance().doShowLayer(FieldBlobDraw.ORE_VEINS_LAYER);
    }

    public static final class Builder {

        private Builder() {}

        public FieldBlobElementRenderer build() {
            FieldBlobDraw.ensureLayerDefaultOn();
            return new FieldBlobElementRenderer(new Object(),
                    new FieldBlobElementRenderProvider(), new FieldBlobElementReader());
        }

        public static Builder begin() {
            return new Builder();
        }
    }
}
