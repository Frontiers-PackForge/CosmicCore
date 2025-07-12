package com.ghostipedia.cosmiccore.client.gui;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.resources.ResourceLocation;

import appeng.client.gui.style.Blitter;

public enum ModifyIcon {

    MULTIPLY_2(0, 0),
    MULTIPLY_3(16, 0),
    MULTIPLY_8(32, 0),
    DIVISION_2(0, 16),
    DIVISION_3(16, 16),
    DIVISION_8(32, 16),
    TOOLBAR_BUTTON_BACKGROUND(32, 32);

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private static final ResourceLocation TEXTURE = CosmicCore.id("textures/gui/states.png");
    private static final int TEXTURE_WIDTH = 48;
    private static final int TEXTURE_HEIGHT = 48;

    ModifyIcon(int x, int y) {
        this.x = x;
        this.y = y;
        width = 16;
        height = 16;
    }

    public Blitter getBlitter() {
        return Blitter.texture(TEXTURE, TEXTURE_WIDTH, TEXTURE_HEIGHT).src(x, y, width, height);
    }
}
