package com.ghostipedia.cosmiccore.client.map.xaero.worldmap;

import com.ghostipedia.cosmiccore.client.map.RevealedFields;
import com.ghostipedia.cosmiccore.client.map.xaero.FieldBlobDraw;
import com.ghostipedia.cosmiccore.client.map.xaero.FieldBlobElement;

import net.minecraft.client.Minecraft;

import xaero.lib.client.gui.widget.Tooltip;
import xaero.map.element.MapElementReader;

public class FieldBlobElementReader extends MapElementReader<FieldBlobElement, Object, FieldBlobElementRenderer> {

    @Override
    public boolean isHidden(FieldBlobElement element, Object context) {
        return false;
    }

    @Override
    public boolean isInteractable(int location, FieldBlobElement element) {
        return true;
    }

    @Override
    public float getBoxScale(int location, FieldBlobElement element, Object context) {
        return 1.0f;
    }

    @Override
    public boolean isHoveredOnMap(int location, FieldBlobElement element, double mouseX, double mouseZ, double scale,
                                  double screenSizeBasedScale, double dimScale, Object context, float partialTicks) {
        double dx = (mouseX - element.field().x() / dimScale) * scale;
        double dz = (mouseZ - element.field().z() / dimScale) * scale;
        float pixelRadius = FieldBlobDraw.zonePixelRadius(
                FieldBlobDraw.zoneBlockRadius(element.field().tier(), element.field().radius()), scale);
        return dx * dx + dz * dz <= (double) pixelRadius * pixelRadius;
    }

    @Override
    public double getRenderX(FieldBlobElement element, Object context, float partialTicks) {
        return element.field().x();
    }

    @Override
    public double getRenderZ(FieldBlobElement element, Object context, float partialTicks) {
        return element.field().z();
    }

    @Override
    public int getInteractionBoxLeft(FieldBlobElement element, Object context, float partialTicks) {
        return -this.getInteractionBoxRight(element, context, partialTicks);
    }

    @Override
    public int getInteractionBoxRight(FieldBlobElement element, Object context, float partialTicks) {
        return zoneExtent(element);
    }

    @Override
    public int getInteractionBoxTop(FieldBlobElement element, Object context, float partialTicks) {
        return -zoneExtent(element);
    }

    @Override
    public int getInteractionBoxBottom(FieldBlobElement element, Object context, float partialTicks) {
        return zoneExtent(element);
    }

    private static int zoneExtent(FieldBlobElement element) {
        return Math.round(FieldBlobDraw.zoneBlockRadius(element.field().tier(), element.field().radius()) *
                (float) FieldBlobDraw.ZONE_SCALE_MULT);
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
    public boolean isRightClickValid(FieldBlobElement element) {
        return false;
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
    public int getRenderBoxLeft(FieldBlobElement element, Object context, float partialTicks) {
        return this.getInteractionBoxLeft(element, context, partialTicks);
    }

    @Override
    public int getRenderBoxRight(FieldBlobElement element, Object context, float partialTicks) {
        return this.getInteractionBoxRight(element, context, partialTicks);
    }

    @Override
    public int getRenderBoxTop(FieldBlobElement element, Object context, float partialTicks) {
        return this.getInteractionBoxTop(element, context, partialTicks);
    }

    @Override
    public int getRenderBoxBottom(FieldBlobElement element, Object context, float partialTicks) {
        return this.getInteractionBoxBottom(element, context, partialTicks);
    }

    @Override
    public Tooltip getTooltip(FieldBlobElement element, Object context, boolean overMenu) {
        boolean depleted = RevealedFields.INSTANCE.isDepleted(element.dimension(), element.field().x(),
                element.field().z());
        return new Tooltip(element.field().displayName() + (depleted ? " (depleted)" : ""));
    }
}
