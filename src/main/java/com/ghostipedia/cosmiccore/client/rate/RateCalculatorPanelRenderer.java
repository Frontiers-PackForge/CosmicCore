package com.ghostipedia.cosmiccore.client.rate;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

final class RateCalculatorPanelRenderer {

    private static final ResourceLocation TEXTURE = CosmicCore.id("textures/gui/rate_calculator/panel.png");
    private static final int SIZE = 62;
    private static final int BORDER = 9;
    private static final int CENTER = SIZE - BORDER * 2;

    private RateCalculatorPanelRenderer() {}

    static void draw(GuiGraphics graphics, int x, int y, int width, int height) {
        int insideWidth = Math.max(0, width - BORDER * 2);
        int insideHeight = Math.max(0, height - BORDER * 2);
        int right = x + BORDER + insideWidth;
        int bottom = y + BORDER + insideHeight;
        blit(graphics, x, y, BORDER, BORDER, 0, 0, BORDER, BORDER);
        blit(graphics, x + BORDER, y, insideWidth, BORDER, BORDER, 0, CENTER, BORDER);
        blit(graphics, right, y, BORDER, BORDER, SIZE - BORDER, 0, BORDER, BORDER);
        blit(graphics, x, y + BORDER, BORDER, insideHeight, 0, BORDER, BORDER, CENTER);
        blit(graphics, x + BORDER, y + BORDER, insideWidth, insideHeight, BORDER, BORDER, CENTER, CENTER);
        blit(graphics, right, y + BORDER, BORDER, insideHeight, SIZE - BORDER, BORDER, BORDER, CENTER);
        blit(graphics, x, bottom, BORDER, BORDER, 0, SIZE - BORDER, BORDER, BORDER);
        blit(graphics, x + BORDER, bottom, insideWidth, BORDER, BORDER, SIZE - BORDER, CENTER, BORDER);
        blit(graphics, right, bottom, BORDER, BORDER, SIZE - BORDER, SIZE - BORDER, BORDER, BORDER);
    }

    private static void blit(GuiGraphics graphics, int x, int y, int width, int height, int sourceX, int sourceY,
                             int sourceWidth, int sourceHeight) {
        if (width > 0 && height > 0) graphics.blit(TEXTURE, x, y, width, height, sourceX, sourceY,
                sourceWidth, sourceHeight, SIZE, SIZE);
    }
}
