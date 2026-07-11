package com.ghostipedia.cosmiccore.client.murkbloom;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import com.mojang.blaze3d.systems.RenderSystem;

public class MurkbloomOverlay implements LayeredDraw.Layer {

    private static final ResourceLocation VIGNETTE_A = CosmicCore.id("textures/gui/murk/murk_vignette_a.png");
    private static final ResourceLocation VIGNETTE_B = CosmicCore.id("textures/gui/murk/murk_vignette_b.png");

    private static final int MURK_RGB = 0x05070C;
    private static final int TEX_SIZE = 512;
    private static final float HOLE_FRAC = 0.20f;
    private static final float COVER_START = 0.5f;
    private static final float GEOM_CLAMP = 0.65f;
    private static final int FRAME_TICKS = 40;

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.options.hideGui) return;
        if (!AbyssClientFog.inHollowWater(mc)) return;

        float intensity = MurkbloomClientState.steadyIntensity();
        float coverage = Mth.clamp((intensity - COVER_START) / (1f - COVER_START), 0f, 1f);
        if (coverage <= 0f) return;

        int w = guiGraphics.guiWidth();
        int h = guiGraphics.guiHeight();

        float geom = Math.min(coverage, GEOM_CLAMP);
        float alpha = 0.55f + 0.45f * coverage;
        int fillColor = ((int) (alpha * 255) << 24) | MURK_RGB;

        float cornerDist = (float) Math.sqrt((double) w * w + (double) h * h) / 2f;
        float holeRadius = cornerDist * (1f - geom);
        int quad = Math.max(2, (int) (holeRadius / HOLE_FRAC * 2f));
        int qx = (w - quad) / 2;
        int qy = (h - quad) / 2;

        long frame = MurkbloomClientState.ticks() / FRAME_TICKS;
        ResourceLocation tex = frame % 2 == 0 ? VIGNETTE_A : VIGNETTE_B;

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        guiGraphics.blit(tex, qx, qy, quad, quad, 0f, 0f, TEX_SIZE, TEX_SIZE, TEX_SIZE, TEX_SIZE);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();

        if (qy > 0) {
            guiGraphics.fill(0, 0, w, qy, fillColor);
            guiGraphics.fill(0, qy + quad, w, h, fillColor);
        }
        if (qx > 0) {
            guiGraphics.fill(0, Math.max(0, qy), qx, Math.min(h, qy + quad), fillColor);
            guiGraphics.fill(qx + quad, Math.max(0, qy), w, Math.min(h, qy + quad), fillColor);
        }
    }
}
