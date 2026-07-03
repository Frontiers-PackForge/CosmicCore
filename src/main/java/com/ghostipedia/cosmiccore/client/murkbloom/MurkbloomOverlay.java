package com.ghostipedia.cosmiccore.client.murkbloom;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.util.Mth;

public class MurkbloomOverlay implements LayeredDraw.Layer {

    private static final int MURK_RGB = 0x0B0F16;
    private static final int FROST_RGB = 0xC9E4EE;
    private static final int SEGMENTS = 14;
    private static final int SIDE_BANDS = 7;

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.options.hideGui) return;
        if (!AbyssClientFog.inHollowWater(mc)) return;

        float intensity = MurkbloomClientState.intensity();
        if (intensity < 0.30f) return;
        long ticks = MurkbloomClientState.ticks();
        int w = guiGraphics.guiWidth();
        int h = guiGraphics.guiHeight();

        float creep = Mth.clamp((intensity - 0.35f) / 0.65f, 0f, 1f);
        float flinchKick = MurkbloomClientState.peekFlinch() * 0.35f;
        creep = Math.min(1f, creep + flinchKick);
        if (creep > 0f) {
            drawMurkFringe(guiGraphics, w, h, creep, ticks);
        }

        float frost = Mth.clamp((intensity - 0.6f) / 0.4f, 0f, 1f);
        if (frost > 0f) {
            drawFrost(guiGraphics, w, h, frost, ticks);
        }

        float taken = Mth.clamp((intensity - 0.82f) / 0.18f, 0f, 1f);
        if (taken > 0f) {
            drawTaken(guiGraphics, w, h, taken, ticks);
        }
    }

    private static void drawMurkFringe(GuiGraphics gg, int w, int h, float creep, long ticks) {
        int maxDepth = (int) (Math.min(w, h) * 0.14f * creep);
        int edgeAlpha = (int) (creep * 150);
        int solid = (edgeAlpha << 24) | MURK_RGB;
        int clear = MURK_RGB;

        int segW = Math.max(1, w / SEGMENTS);
        for (int i = 0; i < SEGMENTS; i++) {
            int x0 = i * segW;
            int x1 = i == SEGMENTS - 1 ? w : x0 + segW;
            int topDepth = undulate(maxDepth, i, ticks, 0.0f);
            int botDepth = undulate(maxDepth, i, ticks, 2.6f);
            if (topDepth > 1) gg.fillGradient(x0, 0, x1, topDepth, solid, clear);
            if (botDepth > 1) gg.fillGradient(x0, h - botDepth, x1, h, clear, solid);
        }

        int segH = Math.max(1, h / SEGMENTS);
        for (int i = 0; i < SEGMENTS; i++) {
            int y0 = i * segH;
            int y1 = i == SEGMENTS - 1 ? h : y0 + segH;
            int leftDepth = undulate(maxDepth, i, ticks, 1.3f);
            int rightDepth = undulate(maxDepth, i, ticks, 3.9f);
            for (int b = 0; b < SIDE_BANDS; b++) {
                float f = 1f - b / (float) SIDE_BANDS;
                int alpha = (int) (edgeAlpha * f * f * 0.55f);
                int color = (alpha << 24) | MURK_RGB;
                int lw = leftDepth * (b + 1) / SIDE_BANDS;
                int rw = rightDepth * (b + 1) / SIDE_BANDS;
                if (lw > 0) gg.fill(0, y0, lw, y1, color);
                if (rw > 0) gg.fill(w - rw, y0, w, y1, color);
            }
        }
    }

    private static void drawFrost(GuiGraphics gg, int w, int h, float frost, long ticks) {
        int reach = (int) (Math.min(w, h) * 0.22f * frost);
        int alpha = (int) (frost * 70);
        int solid = (alpha << 24) | FROST_RGB;
        int clear = FROST_RGB;
        int wobble = (int) (Math.sin(ticks * 0.06) * reach * 0.08f);
        int r = reach + wobble;

        gg.fillGradient(0, 0, r, r / 2, solid, clear);
        gg.fillGradient(w - r, 0, w, r / 2, solid, clear);
        gg.fillGradient(0, h - r / 2, r, h, clear, solid);
        gg.fillGradient(w - r, h - r / 2, w, h, clear, solid);

        int sliver = Math.max(2, r / 7);
        int sAlpha = (int) (frost * 110);
        int sColor = (sAlpha << 24) | FROST_RGB;
        gg.fill(0, 0, sliver, r, sColor);
        gg.fill(w - sliver, 0, w, r, sColor);
        gg.fill(0, h - r, sliver, h, sColor);
        gg.fill(w - sliver, h - r, w, h, sColor);
    }

    private static void drawTaken(GuiGraphics gg, int w, int h, float taken, long ticks) {
        float pulse = 1f + 0.03f * (float) Math.sin(ticks * 0.2);
        int holeW = (int) (w * (1f - 0.92f * taken) * pulse) / 2;
        int holeH = (int) (h * (1f - 0.92f * taken) * pulse) / 2;
        int cx0 = w / 2 - holeW, cx1 = w / 2 + holeW;
        int cy0 = h / 2 - holeH, cy1 = h / 2 + holeH;
        int alpha = (int) (taken * 240);
        int solid = (alpha << 24) | MURK_RGB;
        int soft = ((alpha / 2) << 24) | MURK_RGB;
        int clear = MURK_RGB;

        gg.fill(0, 0, w, Math.max(0, cy0 - 12), solid);
        gg.fill(0, Math.min(h, cy1 + 12), w, h, solid);
        gg.fill(0, cy0 - 12, cx0 - 12, cy1 + 12, solid);
        gg.fill(cx1 + 12, cy0 - 12, w, cy1 + 12, solid);
        gg.fillGradient(cx0 - 12, cy0 - 12, cx1 + 12, cy0, solid, soft);
        gg.fillGradient(cx0 - 12, cy1, cx1 + 12, cy1 + 12, soft, solid);
        gg.fillGradient(cx0 - 12, cy0, cx0, cy1, soft, clear);
        gg.fillGradient(cx1, cy0, cx1 + 12, cy1, clear, soft);
    }

    private static int undulate(int maxDepth, int segment, long ticks, float phaseOffset) {
        float wave = (float) Math.sin(segment * 1.9f + ticks * 0.045f + phaseOffset);
        float wave2 = (float) Math.sin(segment * 3.7f - ticks * 0.028f + phaseOffset * 1.7f);
        return (int) (maxDepth * (0.62f + 0.26f * wave + 0.12f * wave2));
    }
}
