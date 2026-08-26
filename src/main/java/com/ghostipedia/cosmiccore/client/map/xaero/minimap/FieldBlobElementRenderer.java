package com.ghostipedia.cosmiccore.client.map.xaero.minimap;

import com.ghostipedia.cosmiccore.client.map.RevealedField;
import com.ghostipedia.cosmiccore.client.map.RevealedFields;
import com.ghostipedia.cosmiccore.client.map.xaero.FieldBlobDraw;
import com.ghostipedia.cosmiccore.client.map.xaero.FieldBlobElement;
import com.ghostipedia.cosmiccore.mixin.xaerominimap.MinimapElementMapRendererHandlerAccessor;

import com.gregtechceu.gtceu.integration.map.GroupingMapRenderer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;

import xaero.common.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;
import xaero.hud.minimap.element.render.MinimapElementRenderInfo;
import xaero.hud.minimap.element.render.MinimapElementRenderLocation;
import xaero.hud.minimap.element.render.MinimapElementRenderer;
import xaero.hud.minimap.element.render.map.MinimapElementMapRendererHandler;

public class FieldBlobElementRenderer
                                      extends
                                      MinimapElementRenderer<FieldBlobElement, MinimapElementMapRendererHandler> {

    private FieldBlobElementRenderer(FieldBlobElementReader reader, FieldBlobElementRenderProvider provider,
                                     MinimapElementMapRendererHandler context) {
        super(reader, provider, context);
    }

    @Override
    public void preRender(MinimapElementRenderInfo renderInfo, MultiBufferSource.BufferSource renderTypeBuffers,
                          MultiTextureRenderTypeRendererProvider multiTextureRenderTypeRenderers) {
        FieldBlobDraw.beginMinimapBatch();
    }

    @Override
    public boolean renderElement(FieldBlobElement element, boolean highlit, boolean outOfBounds,
                                 double optionalDepth, float optionalScale, double partialX, double partialY,
                                 MinimapElementRenderInfo renderInfo, GuiGraphics graphics,
                                 MultiBufferSource.BufferSource renderTypeBuffers) {
        RevealedField field = element.field();
        boolean depleted = RevealedFields.INSTANCE.isDepleted(element.dimension(), field.x(), field.z());
        MinimapElementMapRendererHandlerAccessor transform = (MinimapElementMapRendererHandlerAccessor) (Object) this.context;
        float pixelRadius = FieldBlobDraw.zonePixelRadius(FieldBlobDraw.zoneBlockRadius(field.tier(), field.radius()),
                transform.cosmiccore$getZoom());
        FieldBlobDraw.addMinimapBlob(graphics.pose().last().pose(), pixelRadius,
                field.colorRGB(), FieldBlobDraw.shapeSeed(field.x(), field.z()), depleted,
                transform.cosmiccore$getTransformPs(), transform.cosmiccore$getTransformPc());
        return true;
    }

    @Override
    public void postRender(MinimapElementRenderInfo renderInfo, MultiBufferSource.BufferSource renderTypeBuffers,
                           MultiTextureRenderTypeRendererProvider multiTextureRenderTypeRenderers) {
        FieldBlobDraw.endMinimapBatch();
    }

    @Override
    public boolean shouldRender(MinimapElementRenderLocation location) {
        return location == MinimapElementRenderLocation.IN_MINIMAP &&
                GroupingMapRenderer.getInstance().doShowLayer(FieldBlobDraw.ORE_VEINS_LAYER);
    }

    public static final class Builder {

        private Builder() {}

        public FieldBlobElementRenderer build(MinimapElementMapRendererHandler handler) {
            FieldBlobDraw.ensureLayerDefaultOn();
            return new FieldBlobElementRenderer(new FieldBlobElementReader(),
                    new FieldBlobElementRenderProvider(), handler);
        }

        public static Builder begin() {
            return new Builder();
        }
    }
}
