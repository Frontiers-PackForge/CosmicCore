package com.ghostipedia.cosmiccore.client.tooltip;

import com.ghostipedia.cosmiccore.mixin.client.tooltip.BakedGlyphAccessor;
import com.ghostipedia.cosmiccore.mixin.client.tooltip.FontAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Style;

public class FoodTooltipClientComponent implements ClientTooltipComponent {

    private static final int LINE_H = 13;
    private static final int ICON_COL = 13;
    private static final int ICON_SIZE = 11;
    private static final int VALUE_GAP = 12;
    private static final float ICON_SCALE = 1.3f;
    private static final int LABEL_COLOR = 0xFF8F86AD;

    private final FoodTooltipComponent data;

    public FoodTooltipClientComponent(FoodTooltipComponent data) {
        this.data = data;
    }

    @Override
    public int getHeight() {
        return data.lines().size() * LINE_H + 2;
    }

    @Override
    public int getWidth(Font font) {
        int max = 0;
        for (FoodTooltipComponent.Line line : data.lines()) {
            int w = ICON_COL + font.width(line.label());
            if (line.value() != null) w += VALUE_GAP + font.width(line.value());
            if (w > max) max = w;
        }
        return max;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        int width = getWidth(font);
        for (int i = 0; i < data.lines().size(); i++) {
            FoodTooltipComponent.Line line = data.lines().get(i);
            int ly = y + i * LINE_H;
            renderIcon(line.icon(), font, guiGraphics, x, ly);
            guiGraphics.drawString(font, line.label(), x + ICON_COL, ly + 2, LABEL_COLOR, false);
            if (line.value() != null) {
                int vw = font.width(line.value());
                guiGraphics.drawString(font, line.value(), x + width - vw, ly + 2, 0xFFFFFFFF, false);
            }
        }
    }

    private static void renderIcon(FoodTooltipComponent.Icon icon, Font font, GuiGraphics guiGraphics, int x, int y) {
        if (icon instanceof FoodTooltipComponent.Icon.Glyph glyph) {
            GlyphBounds bounds = glyphBounds(font, glyph.ch());
            float scale = Math.min(ICON_SCALE,
                    Math.min(ICON_SIZE / bounds.width(), ICON_SIZE / bounds.height()));
            float gx = x + (ICON_COL - bounds.width() * scale) / 2f - bounds.left() * scale;
            float gy = y + (LINE_H - bounds.height() * scale) / 2f - bounds.up() * scale;
            var pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate(gx, gy, 0);
            pose.scale(scale, scale, 1f);
            guiGraphics.drawString(font, glyph.ch(), 0, 0, glyph.color(), false);
            pose.popPose();
        } else if (icon instanceof FoodTooltipComponent.Icon.Effect effect) {
            TextureAtlasSprite sprite = Minecraft.getInstance().getMobEffectTextures().get(effect.effect());
            guiGraphics.blit(x + 1, y + 1, 0, ICON_SIZE, ICON_SIZE, sprite);
        }
    }

    private static GlyphBounds glyphBounds(Font font, String text) {
        FontAccessor fontAccessor = (FontAccessor) font;
        var fontSet = fontAccessor.cosmiccore$invokeGetFontSet(Style.DEFAULT_FONT);
        boolean filterFishyGlyphs = fontAccessor.cosmiccore$getFilterFishyGlyphs();
        float cursor = 0;
        float left = Float.POSITIVE_INFINITY;
        float right = Float.NEGATIVE_INFINITY;
        float up = Float.POSITIVE_INFINITY;
        float down = Float.NEGATIVE_INFINITY;
        var codePoints = text.codePoints().iterator();
        while (codePoints.hasNext()) {
            int codePoint = codePoints.nextInt();
            BakedGlyphAccessor glyph = (BakedGlyphAccessor) fontSet.getGlyph(codePoint);
            left = Math.min(left, cursor + glyph.cosmiccore$getLeft());
            right = Math.max(right, cursor + glyph.cosmiccore$getRight());
            up = Math.min(up, glyph.cosmiccore$getUp());
            down = Math.max(down, glyph.cosmiccore$getDown());
            cursor += fontSet.getGlyphInfo(codePoint, filterFishyGlyphs).getAdvance();
        }
        if (!Float.isFinite(left) || !Float.isFinite(right) || !Float.isFinite(up) || !Float.isFinite(down) ||
                right <= left || down <= up) {
            return new GlyphBounds(0, Math.max(1, font.width(text)), 0, Math.max(1, font.lineHeight));
        }
        return new GlyphBounds(left, right, up, down);
    }

    private record GlyphBounds(float left, float right, float up, float down) {

        private float width() {
            return right - left;
        }

        private float height() {
            return down - up;
        }
    }
}
