package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine.Stage;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

public class WarningOverlayWidget extends Widget {

    private final Supplier<Stage> stageSupplier;

    private float warningPhase = 0f;
    private float alertFlash = 0f;
    private float textGlitch = 0f;

    public WarningOverlayWidget(int x, int y, int width, int height, Supplier<Stage> stageSupplier) {
        super(x, y, width, height);
        this.stageSupplier = stageSupplier;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();

        Stage stage = stageSupplier.get();
        warningPhase += 0.1f;

        if (stage == Stage.DEATH) {
            alertFlash += 0.3f;
            textGlitch = (float) Math.random() * 0.5f;
        } else if (stage == Stage.SUPERSTAR || stage == Stage.BLACK_HOLE) {
            alertFlash += 0.15f;
            textGlitch *= 0.9f;
        } else {
            alertFlash *= 0.9f;
            textGlitch *= 0.8f;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);

        Stage stage = stageSupplier.get();

        if (stage == Stage.EMPTY || stage == Stage.GROWING || stage == Stage.STAR) {
            return;
        }

        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        switch (stage) {
            case SUPERSTAR -> drawSuperstarWarning(graphics, x, y, w, h);
            case BLACK_HOLE -> drawBlackHoleWarning(graphics, x, y, w, h);
            case DEATH -> drawCriticalAlert(graphics, x, y, w, h);
            case DEATH_GRACEFUL -> drawShutdownNotice(graphics, x, y, w, h);
        }
    }

    private void drawSuperstarWarning(GuiGraphics graphics, int x, int y, int w, int h) {
        float pulse = Mth.sin(warningPhase) * 0.5f + 0.5f;

        int bannerAlpha = (int) (0x40 * pulse);
        int bannerColor = (bannerAlpha << 24) | 0xFF8800;

        int bannerY = y + 5;
        int bannerH = 16;
        graphics.fill(x, bannerY, x + w, bannerY + bannerH, bannerColor);

        int borderColor = (0x80 << 24) | 0xFF6600;
        graphics.fill(x, bannerY, x + w, bannerY + 1, borderColor);
        graphics.fill(x, bannerY + bannerH - 1, x + w, bannerY + bannerH, borderColor);

        var font = Minecraft.getInstance().font;
        String warning = "!! CRITICAL MASS APPROACHING !!";
        int textWidth = font.width(warning);

        float scroll = (warningPhase * 2f) % (textWidth + w);
        int textX = x + w - (int) scroll;

        graphics.enableScissor(x, bannerY, x + w, bannerY + bannerH);
        graphics.drawString(font, warning, textX, bannerY + 4, 0xFFFFCC00, false);
        graphics.drawString(font, warning, textX + textWidth + 50, bannerY + 4, 0xFFFFCC00, false);
        graphics.disableScissor();

        drawHazardStripes(graphics, x, bannerY + bannerH + 2, w, 3, 0xFFFF8800, 0xFF442200);
    }

    private void drawBlackHoleWarning(GuiGraphics graphics, int x, int y, int w, int h) {
        float pulse = Mth.sin(warningPhase * 0.7f) * 0.5f + 0.5f;

        int topBannerY = y + 5;
        int bottomBannerY = y + h - 21;
        int bannerH = 16;

        int bannerAlpha = (int) (0x50 * pulse);
        int bannerColor = (bannerAlpha << 24) | 0x8040FF;

        graphics.fill(x, topBannerY, x + w, topBannerY + bannerH, bannerColor);
        graphics.fill(x, bottomBannerY, x + w, bottomBannerY + bannerH, bannerColor);

        int borderColor = (0xA0 << 24) | 0x6020DD;
        graphics.fill(x, topBannerY, x + w, topBannerY + 1, borderColor);
        graphics.fill(x, topBannerY + bannerH - 1, x + w, topBannerY + bannerH, borderColor);
        graphics.fill(x, bottomBannerY, x + w, bottomBannerY + 1, borderColor);
        graphics.fill(x, bottomBannerY + bannerH - 1, x + w, bottomBannerY + bannerH, borderColor);

        var font = Minecraft.getInstance().font;

        String topText = ">> SINGULARITY CONTAINMENT ACTIVE <<";
        int topTextW = font.width(topText);
        graphics.drawString(font, topText, x + (w - topTextW) / 2, topBannerY + 4, 0xFFCC99FF, false);

        String bottomText = "GRAVITATIONAL ANOMALY DETECTED";
        int bottomTextW = font.width(bottomText);
        int glitchOffset = (int) (textGlitch * 3);
        graphics.drawString(font, bottomText, x + (w - bottomTextW) / 2 + glitchOffset, bottomBannerY + 4, 0xFFAA77FF,
                false);

        drawCornerBrackets(graphics, x + 10, topBannerY - 5, w - 20, bannerH + 10, 0xAA8040FF);
    }

    private void drawCriticalAlert(GuiGraphics graphics, int x, int y, int w, int h) {
        float flash = Mth.sin(alertFlash) * 0.5f + 0.5f;

        int screenFlashAlpha = (int) (0x20 * flash);
        graphics.fill(x, y, x + w, y + h, (screenFlashAlpha << 24) | 0xFF0000);

        int topY = y + 5;
        int bottomY = y + h - 25;
        int bannerH = 20;

        int bannerAlpha = (int) (0x60 + 0x40 * flash);
        int bannerColor = (bannerAlpha << 24) | 0xCC0000;

        graphics.fill(x, topY, x + w, topY + bannerH, bannerColor);
        graphics.fill(x, bottomY, x + w, bottomY + bannerH, bannerColor);

        drawHazardStripes(graphics, x, topY - 4, w, 4, 0xFFFF0000, 0xFF440000);
        drawHazardStripes(graphics, x, topY + bannerH, w, 4, 0xFFFF0000, 0xFF440000);
        drawHazardStripes(graphics, x, bottomY - 4, w, 4, 0xFFFF0000, 0xFF440000);
        drawHazardStripes(graphics, x, bottomY + bannerH, w, 4, 0xFFFF0000, 0xFF440000);

        var font = Minecraft.getInstance().font;

        String criticalText = "!!! CRITICAL FAILURE !!!";
        int textW = font.width(criticalText);
        int glitchX = (int) ((Math.random() - 0.5) * textGlitch * 10);
        int glitchY = (int) ((Math.random() - 0.5) * textGlitch * 4);

        int textColor = flash > 0.5f ? 0xFFFFFFFF : 0xFFFF4444;
        graphics.drawString(font, criticalText, x + (w - textW) / 2 + glitchX, topY + 6 + glitchY, textColor, false);

        if (textGlitch > 0.2f) {
            int ghostAlpha = (int) (0x40 * textGlitch);
            int ghostColor = (ghostAlpha << 24) | 0x00FFFF;
            graphics.drawString(font, criticalText, x + (w - textW) / 2 + glitchX + 2, topY + 6 + glitchY, ghostColor,
                    false);
        }

        String evacuateText = "EVACUATE IMMEDIATELY";
        int evacW = font.width(evacuateText);
        graphics.drawString(font, evacuateText, x + (w - evacW) / 2, bottomY + 6, 0xFFFFAAAA, false);

        drawWarningTriangles(graphics, x + 15, topY + 3, 14);
        drawWarningTriangles(graphics, x + w - 29, topY + 3, 14);
    }

    private void drawShutdownNotice(GuiGraphics graphics, int x, int y, int w, int h) {
        float fade = Mth.sin(warningPhase * 0.3f) * 0.3f + 0.7f;

        int bannerY = y + h / 2 - 12;
        int bannerH = 24;

        int bannerAlpha = (int) (0x50 * fade);
        int bannerColor = (bannerAlpha << 24) | 0x604040;
        graphics.fill(x + 20, bannerY, x + w - 20, bannerY + bannerH, bannerColor);

        int borderColor = (0x60 << 24) | 0x804040;
        graphics.fill(x + 20, bannerY, x + w - 20, bannerY + 1, borderColor);
        graphics.fill(x + 20, bannerY + bannerH - 1, x + w - 20, bannerY + bannerH, borderColor);
        graphics.fill(x + 20, bannerY, x + 21, bannerY + bannerH, borderColor);
        graphics.fill(x + w - 21, bannerY, x + w - 20, bannerY + bannerH, borderColor);

        var font = Minecraft.getInstance().font;
        String text = "CONTROLLED SHUTDOWN IN PROGRESS";
        int textW = font.width(text);
        int textColor = (int) (0xFF * fade) << 24 | 0x999999;
        graphics.drawString(font, text, x + (w - textW) / 2, bannerY + 8, textColor, false);

        int dotsVisible = ((int) (warningPhase * 2)) % 4;
        StringBuilder dots = new StringBuilder();
        for (int i = 0; i < dotsVisible; i++) dots.append(".");
        graphics.drawString(font, dots.toString(), x + (w + textW) / 2 + 2, bannerY + 8, textColor, false);
    }

    private void drawHazardStripes(GuiGraphics graphics, int x, int y, int w, int h, int color1, int color2) {
        int stripeWidth = 8;
        float offset = (warningPhase * 20) % (stripeWidth * 2);

        graphics.enableScissor(x, y, x + w, y + h);
        for (int sx = x - stripeWidth * 2 + (int) offset; sx < x + w + stripeWidth; sx += stripeWidth * 2) {
            graphics.fill(sx, y, sx + stripeWidth, y + h, color1);
            graphics.fill(sx + stripeWidth, y, sx + stripeWidth * 2, y + h, color2);
        }
        graphics.disableScissor();
    }

    private void drawCornerBrackets(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        int len = 8;
        int thickness = 2;

        graphics.fill(x, y, x + len, y + thickness, color);
        graphics.fill(x, y, x + thickness, y + len, color);

        graphics.fill(x + w - len, y, x + w, y + thickness, color);
        graphics.fill(x + w - thickness, y, x + w, y + len, color);

        graphics.fill(x, y + h - thickness, x + len, y + h, color);
        graphics.fill(x, y + h - len, x + thickness, y + h, color);

        graphics.fill(x + w - len, y + h - thickness, x + w, y + h, color);
        graphics.fill(x + w - thickness, y + h - len, x + w, y + h, color);
    }

    private void drawWarningTriangles(GuiGraphics graphics, int x, int y, int size) {
        int color = 0xFFFFCC00;

        for (int row = 0; row < size; row++) {
            int halfWidth = row * size / (size * 2);
            int cx = x + size / 2;
            int drawY = y + row;
            graphics.fill(cx - halfWidth, drawY, cx + halfWidth + 1, drawY + 1, color);
        }

        var font = Minecraft.getInstance().font;
        graphics.drawString(font, "!", x + size / 2 - 2, y + size / 3, 0xFF000000, false);
    }
}
