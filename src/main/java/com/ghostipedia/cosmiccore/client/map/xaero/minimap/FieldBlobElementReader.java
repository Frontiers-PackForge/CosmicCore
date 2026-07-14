package com.ghostipedia.cosmiccore.client.map.xaero.minimap;

import com.ghostipedia.cosmiccore.client.map.xaero.FieldBlobDraw;
import com.ghostipedia.cosmiccore.client.map.xaero.FieldBlobElement;

import net.minecraft.client.Minecraft;

import xaero.hud.minimap.element.render.MinimapElementReader;
import xaero.hud.minimap.element.render.map.MinimapElementMapRendererHandler;

public class FieldBlobElementReader extends MinimapElementReader<FieldBlobElement, MinimapElementMapRendererHandler> {

    @Override
    public boolean isHidden(FieldBlobElement element, MinimapElementMapRendererHandler context) {
        return false;
    }

    @Override
    public double getRenderX(FieldBlobElement element, MinimapElementMapRendererHandler context, float partialTicks) {
        return element.field().x();
    }

    @Override
    public double getRenderY(FieldBlobElement element, MinimapElementMapRendererHandler context, float partialTicks) {
        return 0;
    }

    @Override
    public double getRenderZ(FieldBlobElement element, MinimapElementMapRendererHandler context, float partialTicks) {
        return element.field().z();
    }

    @Override
    public int getInteractionBoxLeft(FieldBlobElement element, MinimapElementMapRendererHandler context,
                                     float partialTicks) {
        return -this.getInteractionBoxRight(element, context, partialTicks);
    }

    @Override
    public int getInteractionBoxRight(FieldBlobElement element, MinimapElementMapRendererHandler context,
                                      float partialTicks) {
        return extent(element);
    }

    @Override
    public int getInteractionBoxTop(FieldBlobElement element, MinimapElementMapRendererHandler context,
                                    float partialTicks) {
        return -extent(element);
    }

    @Override
    public int getInteractionBoxBottom(FieldBlobElement element, MinimapElementMapRendererHandler context,
                                       float partialTicks) {
        return extent(element);
    }

    private static int extent(FieldBlobElement element) {
        return Math.round(FieldBlobDraw.minimapZoneRadius(element.field().tier(), element.field().radius()));
    }

    @Override
    public int getLeftSideLength(FieldBlobElement element, Minecraft mc) {
        return 9 + mc.font.width(element.field().displayName());
    }

    @Override
    public String getMenuName(FieldBlobElement element) {
        return element.field().displayName();
    }

    @Override
    public int getMenuTextFillLeftPadding(FieldBlobElement element) {
        return 0;
    }

    @Override
    public String getFilterName(FieldBlobElement element) {
        return this.getMenuName(element);
    }

    @Override
    public int getRightClickTitleBackgroundColor(FieldBlobElement element) {
        return element.field().colorRGB();
    }

    @Override
    public boolean shouldScaleBoxWithOptionalScale() {
        return true;
    }

    @Override
    public int getRenderBoxLeft(FieldBlobElement element, MinimapElementMapRendererHandler context,
                                float partialTicks) {
        return this.getInteractionBoxLeft(element, context, partialTicks);
    }

    @Override
    public int getRenderBoxRight(FieldBlobElement element, MinimapElementMapRendererHandler context,
                                 float partialTicks) {
        return this.getInteractionBoxRight(element, context, partialTicks);
    }

    @Override
    public int getRenderBoxTop(FieldBlobElement element, MinimapElementMapRendererHandler context,
                               float partialTicks) {
        return this.getInteractionBoxTop(element, context, partialTicks);
    }

    @Override
    public int getRenderBoxBottom(FieldBlobElement element, MinimapElementMapRendererHandler context,
                                  float partialTicks) {
        return this.getInteractionBoxBottom(element, context, partialTicks);
    }
}
