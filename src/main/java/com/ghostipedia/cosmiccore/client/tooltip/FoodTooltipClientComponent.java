package com.ghostipedia.cosmiccore.client.tooltip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class FoodTooltipClientComponent implements ClientTooltipComponent {

    private static final int LINE_H = 13;
    private static final int ICON_COL = 13;
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
            float gy = y + (LINE_H - font.lineHeight * ICON_SCALE) / 2f;
            var pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate(x, gy, 0);
            pose.scale(ICON_SCALE, ICON_SCALE, 1f);
            guiGraphics.drawString(font, glyph.ch(), 0, 0, glyph.color(), false);
            pose.popPose();
        } else if (icon instanceof FoodTooltipComponent.Icon.Effect effect) {
            TextureAtlasSprite sprite = Minecraft.getInstance().getMobEffectTextures().get(effect.effect());
            guiGraphics.blit(x, y + (LINE_H - 10) / 2, 0, 10, 10, sprite);
        }
    }
}
