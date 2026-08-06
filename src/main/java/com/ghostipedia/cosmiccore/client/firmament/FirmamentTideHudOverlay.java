package com.ghostipedia.cosmiccore.client.firmament;

import com.ghostipedia.cosmiccore.client.gui.MajorInfoPanelRenderer;
import com.ghostipedia.cosmiccore.common.dimension.FirmamentDimension;
import com.ghostipedia.cosmiccore.common.network.packet.FirmamentTideHudPacket;

import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class FirmamentTideHudOverlay implements LayeredDraw.Layer {

    private static final Component TITLE = Component.translatable("cosmiccore.firmament.tide.title");
    private static final Component PROMPT = Component.translatable("cosmiccore.firmament.tide.prompt");
    private static final int WIDTH = 204;
    private static final int HEIGHT = 62;
    private static final int BAR_WIDTH = 144;
    private static final long FADE_IN_MILLIS = 700L;
    private static final long FADE_OUT_MILLIS = 900L;

    private static byte mode = FirmamentTideHudPacket.HIDDEN;
    private static float progress;
    private static long transitionStartedAt;
    private static long arrivedAt;

    public static void setState(byte nextMode, float holdProgress) {
        long now = Util.getMillis();
        if ((nextMode == FirmamentTideHudPacket.RETURNING || nextMode == FirmamentTideHudPacket.ASCENDING) &&
                mode != nextMode) {
            transitionStartedAt = now;
        }
        if ((nextMode == FirmamentTideHudPacket.ARRIVED || nextMode == FirmamentTideHudPacket.ENTERED) &&
                mode != nextMode) {
            arrivedAt = now;
        }
        mode = nextMode;
        progress = Math.clamp(holdProgress, 0.0f, 1.0f);
    }

    public static boolean isReturning() {
        return mode == FirmamentTideHudPacket.RETURNING;
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        long now = Util.getMillis();
        int fadeAlpha = fadeAlpha(now);
        boolean panelVisible = mode == FirmamentTideHudPacket.PROMPT ||
                mode == FirmamentTideHudPacket.RETURNING;
        if (panelVisible && !minecraft.level.dimension().equals(FirmamentDimension.KEY)) {
            if (mode == FirmamentTideHudPacket.PROMPT) setState(FirmamentTideHudPacket.HIDDEN, 0.0f);
            panelVisible = false;
        }

        if (panelVisible && !minecraft.options.hideGui && !minecraft.gui.getDebugOverlay().showDebugScreen()) {
            drawPanel(guiGraphics, minecraft);
        }
        if (fadeAlpha > 0) {
            guiGraphics.fill(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), fadeAlpha << 24 | 0x080A18);
        }
    }

    private static void drawPanel(GuiGraphics guiGraphics, Minecraft minecraft) {
        int left = guiGraphics.guiWidth() / 2 - WIDTH / 2;
        int top = 58;
        MajorInfoPanelRenderer.draw(guiGraphics, left, top, WIDTH, HEIGHT);
        int center = guiGraphics.guiWidth() / 2;
        guiGraphics.drawCenteredString(minecraft.font, TITLE, center, top + 13, 0xFFFFC36A);
        guiGraphics.drawCenteredString(minecraft.font, PROMPT, center, top + 28, 0xFFD8C8E8);

        int barLeft = center - BAR_WIDTH / 2;
        int barTop = top + 45;
        guiGraphics.fill(barLeft - 1, barTop - 1, barLeft + BAR_WIDTH + 1, barTop + 5, 0xB00A0712);
        guiGraphics.fill(barLeft, barTop, barLeft + BAR_WIDTH, barTop + 4, 0xC0201829);
        int filled = Math.round(BAR_WIDTH * progress);
        if (filled > 0) {
            guiGraphics.fill(barLeft, barTop, barLeft + filled, barTop + 4, 0xFFFFA95C);
            guiGraphics.fill(barLeft + Math.max(0, filled - 2), barTop, barLeft + filled, barTop + 4, 0xFFD889FF);
        }
    }

    private static int fadeAlpha(long now) {
        if (mode == FirmamentTideHudPacket.RETURNING || mode == FirmamentTideHudPacket.ASCENDING) {
            return Mth.floor(255.0f * smooth((float) (now - transitionStartedAt) / FADE_IN_MILLIS));
        }
        if (mode != FirmamentTideHudPacket.ARRIVED && mode != FirmamentTideHudPacket.ENTERED) return 0;
        float elapsed = (float) (now - arrivedAt) / FADE_OUT_MILLIS;
        if (elapsed >= 1.0f) {
            setState(FirmamentTideHudPacket.HIDDEN, 0.0f);
            return 0;
        }
        return Mth.floor(255.0f * (1.0f - smooth(elapsed)));
    }

    private static float smooth(float value) {
        float clamped = Math.clamp(value, 0.0f, 1.0f);
        return clamped * clamped * (3.0f - 2.0f * clamped);
    }
}
