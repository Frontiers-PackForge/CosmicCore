package com.ghostipedia.cosmiccore.client.gui;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class MajorInfoPanelRenderer {

    private static final ResourceLocation TEXTURE = CosmicCore.id("textures/gui/overlay/major_info_panel.png");
    private static final int TEXTURE_SIZE = 62;
    private static final int BORDER = 9;
    private static final int CENTER = TEXTURE_SIZE - BORDER * 2;

    private MajorInfoPanelRenderer() {}

    public static void draw(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        int innerWidth = Math.max(0, width - BORDER * 2);
        int innerHeight = Math.max(0, height - BORDER * 2);
        int right = x + BORDER + innerWidth;
        int bottom = y + BORDER + innerHeight;

        blit(guiGraphics, x, y, BORDER, BORDER, 0, 0, BORDER, BORDER);
        blit(guiGraphics, x + BORDER, y, innerWidth, BORDER, BORDER, 0, CENTER, BORDER);
        blit(guiGraphics, right, y, BORDER, BORDER, TEXTURE_SIZE - BORDER, 0, BORDER, BORDER);
        blit(guiGraphics, x, y + BORDER, BORDER, innerHeight, 0, BORDER, BORDER, CENTER);
        blit(guiGraphics, x + BORDER, y + BORDER, innerWidth, innerHeight, BORDER, BORDER, CENTER, CENTER);
        blit(guiGraphics, right, y + BORDER, BORDER, innerHeight,
                TEXTURE_SIZE - BORDER, BORDER, BORDER, CENTER);
        blit(guiGraphics, x, bottom, BORDER, BORDER, 0, TEXTURE_SIZE - BORDER, BORDER, BORDER);
        blit(guiGraphics, x + BORDER, bottom, innerWidth, BORDER,
                BORDER, TEXTURE_SIZE - BORDER, CENTER, BORDER);
        blit(guiGraphics, right, bottom, BORDER, BORDER,
                TEXTURE_SIZE - BORDER, TEXTURE_SIZE - BORDER, BORDER, BORDER);
    }

    private static void blit(
                             GuiGraphics guiGraphics,
                             int x,
                             int y,
                             int width,
                             int height,
                             int sourceX,
                             int sourceY,
                             int sourceWidth,
                             int sourceHeight) {
        if (width <= 0 || height <= 0) return;
        guiGraphics.blit(TEXTURE, x, y, width, height, sourceX, sourceY,
                sourceWidth, sourceHeight, TEXTURE_SIZE, TEXTURE_SIZE);
    }
}
