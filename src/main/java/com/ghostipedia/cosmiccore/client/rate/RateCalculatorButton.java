package com.ghostipedia.cosmiccore.client.rate;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

final class RateCalculatorButton extends Button {

    private static final ResourceLocation NORMAL = CosmicCore.id("textures/gui/rate_calculator/button.png");
    private static final ResourceLocation HIGHLIGHTED = CosmicCore
            .id("textures/gui/rate_calculator/button_highlighted.png");
    private static final ResourceLocation DISABLED = CosmicCore.id("textures/gui/rate_calculator/button_disabled.png");

    private RateCalculatorButton(int x, int y, int width, Component message, OnPress onPress) {
        super(x, y, width, 20, message, onPress, DEFAULT_NARRATION);
    }

    static RateCalculatorButton create(int x, int y, int width, Component message, OnPress onPress) {
        return new RateCalculatorButton(x, y, width, message, onPress);
    }

    static void drawRow(GuiGraphics graphics, int x, int y, int width, int height, boolean highlighted) {
        drawNineSlice(graphics, highlighted ? HIGHLIGHTED : NORMAL, x, y, width, height, 3);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation texture = !active ? DISABLED : isHoveredOrFocused() ? HIGHLIGHTED : NORMAL;
        int border = active ? 3 : 1;
        drawNineSlice(graphics, texture, getX(), getY(), width, height, border);
        renderScrollingString(graphics, Minecraft.getInstance().font, 2, active ? 0xFFFFFFFF : 0xFFA0A0A0);
    }

    private static void drawNineSlice(GuiGraphics g, ResourceLocation t, int x, int y, int w, int h, int b) {
        int cw = Math.max(0, w - b * 2), ch = Math.max(0, h - b * 2), r = x + b + cw, d = y + b + ch, c = 200 - b * 2;
        blit(g, t, x, y, b, b, 0, 0, b, b);
        blit(g, t, x + b, y, cw, b, b, 0, c, b);
        blit(g, t, r, y, b, b, 200 - b, 0, b, b);
        blit(g, t, x, y + b, b, ch, 0, b, b, 20 - b * 2);
        blit(g, t, x + b, y + b, cw, ch, b, b, c, 20 - b * 2);
        blit(g, t, r, y + b, b, ch, 200 - b, b, b, 20 - b * 2);
        blit(g, t, x, d, b, b, 0, 20 - b, b, b);
        blit(g, t, x + b, d, cw, b, b, 20 - b, c, b);
        blit(g, t, r, d, b, b, 200 - b, 20 - b, b, b);
    }

    private static void blit(GuiGraphics g, ResourceLocation t, int x, int y, int w, int h, int sx, int sy, int sw,
                             int sh) {
        if (w > 0 && h > 0) g.blit(t, x, y, w, h, sx, sy, sw, sh, 200, 20);
    }
}
