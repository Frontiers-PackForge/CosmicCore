package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine.Stage;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class HolographicScanlineWidget extends Widget {

    private final Supplier<Stage> stageSupplier;
    private float scanY = 0f;
    private float glitchTimer = 0f;
    private float interferencePhase = 0f;

    public HolographicScanlineWidget(int x, int y, int width, int height, Supplier<Stage> stageSupplier) {
        super(x, y, width, height);
        this.stageSupplier = stageSupplier;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();

        Stage stage = stageSupplier.get();
        float scanSpeed = getScanSpeed(stage);
        scanY = (scanY + scanSpeed) % 1f;

        interferencePhase += 0.07f;

        if (stage == Stage.DEATH || stage == Stage.BLACK_HOLE) {
            glitchTimer += 0.15f;
        } else {
            glitchTimer *= 0.95f;
        }
    }

    private float getScanSpeed(Stage stage) {
        return switch (stage) {
            case EMPTY -> 0.003f;
            case GROWING -> 0.008f;
            case STAR -> 0.005f;
            case SUPERSTAR -> 0.012f;
            case BLACK_HOLE -> 0.02f;
            case DEATH -> 0.04f;
            case DEATH_GRACEFUL -> 0.002f;
        };
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);

        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        Stage stage = stageSupplier.get();
        int baseColor = getStageColor(stage);

        drawScanLines(graphics, x, y, w, h, baseColor);
        drawMainScanBeam(graphics, x, y, w, h, baseColor);
        drawInterferencePattern(graphics, x, y, w, h, stage);

        if (glitchTimer > 0.5f) {
            drawGlitchEffect(graphics, x, y, w, h, stage);
        }

        drawEdgeVignette(graphics, x, y, w, h, baseColor);
    }

    private void drawScanLines(GuiGraphics graphics, int x, int y, int w, int h, int baseColor) {
        int lineAlpha = 0x08;
        int lineColor = (lineAlpha << 24) | (baseColor & 0x00FFFFFF);

        for (int ly = y; ly < y + h; ly += 2) {
            graphics.fill(x, ly, x + w, ly + 1, lineColor);
        }
    }

    private void drawMainScanBeam(GuiGraphics graphics, int x, int y, int w, int h, int baseColor) {
        int beamY = y + (int) (h * scanY);
        int beamHeight = 3;

        for (int i = 0; i < 8; i++) {
            int spread = i * 2;
            float falloff = 1f - (i / 8f);
            int alpha = (int) (0x40 * falloff);
            int color = (alpha << 24) | (baseColor & 0x00FFFFFF);

            int drawY = beamY - spread;
            if (drawY >= y && drawY < y + h) {
                graphics.fill(x, drawY, x + w, drawY + beamHeight, color);
            }
            drawY = beamY + spread;
            if (drawY >= y && drawY < y + h) {
                graphics.fill(x, drawY, x + w, drawY + beamHeight, color);
            }
        }

        int coreAlpha = 0x80;
        int coreColor = (coreAlpha << 24) | 0xFFFFFF;
        if (beamY >= y && beamY < y + h - beamHeight) {
            graphics.fill(x, beamY, x + w, beamY + beamHeight, coreColor);
        }
    }

    private void drawInterferencePattern(GuiGraphics graphics, int x, int y, int w, int h, Stage stage) {
        if (stage == Stage.EMPTY || stage == Stage.DEATH_GRACEFUL) return;

        float intensity = switch (stage) {
            case GROWING -> 0.3f;
            case STAR -> 0.2f;
            case SUPERSTAR -> 0.5f;
            case BLACK_HOLE -> 0.7f;
            case DEATH -> 0.9f;
            default -> 0f;
        };

        int bands = 3 + (int) (intensity * 5);
        for (int i = 0; i < bands; i++) {
            float bandPhase = interferencePhase + i * 0.7f;
            float bandY = (Mth.sin(bandPhase) * 0.5f + 0.5f);
            int by = y + (int) (h * bandY);

            float bandIntensity = Mth.sin(bandPhase * 2.3f) * 0.5f + 0.5f;
            int alpha = (int) (0x15 * intensity * bandIntensity);
            int color = (alpha << 24) | 0x00FFFF;

            if (by >= y && by < y + h - 2) {
                graphics.fill(x, by, x + w, by + 2, color);
            }
        }
    }

    private void drawGlitchEffect(GuiGraphics graphics, int x, int y, int w, int h, Stage stage) {
        long time = System.currentTimeMillis();
        int glitchCount = stage == Stage.DEATH ? 8 : 3;

        for (int i = 0; i < glitchCount; i++) {
            if (((time / 50) + i * 17) % 7 < 2) {
                int glitchY = y + (int) ((time / 30 + i * 43) % h);
                int glitchH = 2 + (int) (Math.random() * 4);
                int offsetX = (int) ((Math.random() - 0.5) * 10);

                int glitchColor = stage == Stage.DEATH ? 0x40FF0000 : 0x30FF00FF;
                graphics.fill(x + offsetX, glitchY, x + w + offsetX, Math.min(glitchY + glitchH, y + h), glitchColor);
            }
        }
    }

    private void drawEdgeVignette(GuiGraphics graphics, int x, int y, int w, int h, int baseColor) {
        int vignetteSize = 15;
        for (int i = 0; i < vignetteSize; i++) {
            float progress = (float) i / vignetteSize;
            int alpha = (int) (0x30 * (1f - progress));
            int color = (alpha << 24) | (baseColor & 0x00FFFFFF);

            graphics.fill(x, y + i, x + w, y + i + 1, color);
            graphics.fill(x, y + h - i - 1, x + w, y + h - i, color);
            graphics.fill(x + i, y, x + i + 1, y + h, color);
            graphics.fill(x + w - i - 1, y, x + w - i, y + h, color);
        }
    }

    private int getStageColor(Stage stage) {
        return switch (stage) {
            case EMPTY -> 0x4080A0;
            case GROWING -> 0x60A0FF;
            case STAR -> 0xFFCC44;
            case SUPERSTAR -> 0xFF6622;
            case BLACK_HOLE -> 0xAA44FF;
            case DEATH -> 0xFF2020;
            case DEATH_GRACEFUL -> 0x664444;
        };
    }
}
