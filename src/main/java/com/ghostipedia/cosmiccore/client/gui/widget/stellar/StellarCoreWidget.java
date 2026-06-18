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

public class StellarCoreWidget extends Widget {

    private final Supplier<Stage> stageSupplier;
    private final java.util.function.IntSupplier customColorSupplier;

    private float animPhase = 0f;
    private float pulsePhase = 0f;
    private float transitionProgress = 1f;
    private Stage previousStage = Stage.EMPTY;
    private Stage targetStage = Stage.EMPTY;

    private float prestigeScale = 1f;
    private float prestigeAlpha = 1f;
    private boolean prestigeAnimating = false;

    public StellarCoreWidget(int x, int y, int size, Supplier<Stage> stageSupplier) {
        this(x, y, size, stageSupplier, null);
    }

    public StellarCoreWidget(int x, int y, int size, Supplier<Stage> stageSupplier,
                             java.util.function.IntSupplier customColorSupplier) {
        super(x, y, size, size);
        this.stageSupplier = stageSupplier;
        this.customColorSupplier = customColorSupplier;
    }

    private int getCustomColor() {
        return customColorSupplier != null ? customColorSupplier.getAsInt() : -1;
    }

    public void setPrestigeScale(float scale) {
        this.prestigeScale = Mth.clamp(scale, 0f, 1f);
    }

    public void setPrestigeAlpha(float alpha) {
        this.prestigeAlpha = Mth.clamp(alpha, 0f, 1f);
    }

    public void setPrestigeAnimating(boolean animating) {
        this.prestigeAnimating = animating;
        if (!animating) {
            this.prestigeScale = 1f;
            this.prestigeAlpha = 1f;
        }
    }

    public boolean isPrestigeAnimating() {
        return prestigeAnimating;
    }

    public float getPrestigeScale() {
        return prestigeScale;
    }

    public float getPrestigeAlpha() {
        return prestigeAlpha;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        animPhase += 0.03f;
        pulsePhase += 0.08f;

        Stage current = stageSupplier.get();
        if (current != targetStage) {
            previousStage = targetStage;
            targetStage = current;
            transitionProgress = 0f;
        }

        if (transitionProgress < 1f) {
            transitionProgress = Math.min(1f, transitionProgress + 0.02f);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);

        if (prestigeAnimating && prestigeAlpha <= 0f) {
            return;
        }

        int cx = getPosition().x + getSize().width / 2;
        int cy = getPosition().y + getSize().height / 2;
        int maxRadius = getSize().width / 2 - 5;

        if (prestigeAnimating) {
            maxRadius = (int) (maxRadius * prestigeScale);
        }

        Stage stage = stageSupplier.get();

        drawVoidBackground(graphics, cx, cy, getSize().width / 2 - 5);

        switch (stage) {
            case EMPTY -> drawEmptyCore(graphics, cx, cy, maxRadius);
            case GROWING -> drawGrowingCore(graphics, cx, cy, maxRadius);
            case STAR -> drawStarCore(graphics, cx, cy, maxRadius);
            case SUPERSTAR -> drawSuperstarCore(graphics, cx, cy, maxRadius);
            case BLACK_HOLE -> drawBlackHoleCore(graphics, cx, cy, maxRadius);
            case DEATH, DEATH_GRACEFUL -> drawDeathCore(graphics, cx, cy, maxRadius, stage);
        }

        if (!prestigeAnimating) {
            drawStageLabel(graphics, cx, stage);
        }
    }

    private void drawVoidBackground(GuiGraphics graphics, int cx, int cy, int radius) {
        for (int r = radius; r > 0; r -= 3) {
            float progress = (float) r / radius;
            int alpha = (int) (30 * progress);
            int color = (alpha << 24) | 0x101020;
            drawCircle(graphics, cx, cy, r, color);
        }
    }

    private void drawEmptyCore(GuiGraphics graphics, int cx, int cy, int radius) {
        float pulse = 0.5f + 0.3f * Mth.sin(pulsePhase);
        int alpha = (int) (pulse * 60);

        drawCircleRing(graphics, cx, cy, radius - 5, 2, (alpha << 24) | 0x404060);

        int innerRadius = radius / 3;
        for (int r = innerRadius; r > 0; r -= 2) {
            float glowProgress = (float) r / innerRadius;
            int glowAlpha = (int) (20 * glowProgress * pulse);
            drawCircle(graphics, cx, cy, r, (glowAlpha << 24) | 0x303050);
        }

        drawCircle(graphics, cx, cy, 3, (int) (pulse * 100) << 24 | 0x505080);
    }

    private void drawGrowingCore(GuiGraphics graphics, int cx, int cy, int radius) {
        float pulse = 0.8f + 0.2f * Mth.sin(pulsePhase * 1.5f);
        float grow = 0.3f + 0.4f * (Mth.sin(animPhase * 0.5f) * 0.5f + 0.5f);

        int coreRadius = (int) (radius * grow * pulse);

        for (int i = 0; i < 8; i++) {
            float angle = animPhase * 2f + i * Mth.PI / 4f;
            float dist = radius * 0.8f * (0.5f + 0.5f * Mth.sin(animPhase + i));
            int px = cx + (int) (Mth.cos(angle) * dist);
            int py = cy + (int) (Mth.sin(angle) * dist);
            int pAlpha = (int) (100 * (1f - dist / (radius * 0.8f)));
            drawCircle(graphics, px, py, 2, (pAlpha << 24) | 0x6090FF);
        }

        int[] colors = { 0x2040A0, 0x4060C0, 0x6080E0, 0x80A0FF };
        for (int layer = 0; layer < colors.length; layer++) {
            int layerRadius = coreRadius - layer * 3;
            if (layerRadius > 0) {
                int alpha = 60 + layer * 30;
                drawCircle(graphics, cx, cy, layerRadius, (alpha << 24) | colors[layer]);
            }
        }

        drawCircle(graphics, cx, cy, Math.max(3, coreRadius / 4), 0xDDFFFFFF);
    }

    private void drawStarCore(GuiGraphics graphics, int cx, int cy, int radius) {
        float pulse = 0.95f + 0.05f * Mth.sin(pulsePhase);
        int coreRadius = (int) (radius * 0.7f * pulse);

        int customColor = getCustomColor();
        int baseColor = customColor != -1 ? customColor : 0xFFCC44;

        int[] colors = generateColorGradient(baseColor);
        int coronaColor = blendTowardsWhite(baseColor, 0.3f);

        for (int r = coreRadius + 15; r > coreRadius; r -= 2) {
            float glowProgress = (float) (r - coreRadius) / 15f;
            int alpha = (int) ((1f - glowProgress) * 60);
            drawCircle(graphics, cx, cy, r, (alpha << 24) | coronaColor);
        }

        for (int layer = 0; layer < colors.length; layer++) {
            int layerRadius = coreRadius - layer * (coreRadius / colors.length);
            if (layerRadius > 0) {
                int alpha = 180 + layer * 15;
                alpha = Math.min(255, alpha);
                drawCircle(graphics, cx, cy, layerRadius, (alpha << 24) | colors[layer]);
            }
        }

        int hotCenterRadius = coreRadius / 4;
        drawCircle(graphics, cx, cy, hotCenterRadius, 0xEEFFFFFF);

        drawSolarFlares(graphics, cx, cy, coreRadius, coronaColor);
    }

    private void drawSuperstarCore(GuiGraphics graphics, int cx, int cy, int radius) {
        float pulse = 0.9f + 0.1f * Mth.sin(pulsePhase * 0.7f);
        int coreRadius = (int) (radius * 0.85f * pulse);

        int customColor = getCustomColor();
        int baseColor = customColor != -1 ? shiftHue(customColor, 0.05f) : 0xFF7722;

        int[] colors = generateColorGradient(baseColor);
        int coronaColor = darken(baseColor, 0.7f);

        for (int r = coreRadius + 20; r > coreRadius; r -= 2) {
            float glowProgress = (float) (r - coreRadius) / 20f;
            int alpha = (int) ((1f - glowProgress) * 80);
            drawCircle(graphics, cx, cy, r, (alpha << 24) | coronaColor);
        }

        for (int layer = 0; layer < colors.length; layer++) {
            int layerRadius = coreRadius - layer * (coreRadius / colors.length);
            if (layerRadius > 0) {
                int alpha = 200 + layer * 11;
                alpha = Math.min(255, alpha);
                drawCircle(graphics, cx, cy, layerRadius, (alpha << 24) | colors[layer]);
            }
        }

        drawCircle(graphics, cx, cy, coreRadius / 3, 0xFFFFEECC);

        drawSolarFlares(graphics, cx, cy, coreRadius, coronaColor);
        drawSolarFlares(graphics, cx, cy, coreRadius * 0.8f, blendTowardsWhite(baseColor, 0.5f));
    }

    private void drawBlackHoleCore(GuiGraphics graphics, int cx, int cy, int radius) {
        int eventHorizonRadius = (int) (radius * 0.3f);
        drawCircle(graphics, cx, cy, eventHorizonRadius, 0xFF000000);

        float diskPulse = 0.9f + 0.1f * Mth.sin(pulsePhase * 0.5f);
        for (int i = 0; i < 360; i += 5) {
            float angle = Mth.DEG_TO_RAD * i + animPhase;
            float diskRadius = radius * 0.7f * diskPulse;
            float variance = 0.1f * Mth.sin(angle * 3 + animPhase * 2);
            diskRadius *= (1f + variance);

            int px = cx + (int) (Mth.cos(angle) * diskRadius);
            int py = cy + (int) (Mth.sin(angle) * diskRadius * 0.3f);

            float colorPhase = (i / 360f + animPhase * 0.1f) % 1f;
            int r = (int) (128 + 127 * Mth.sin(colorPhase * Mth.TWO_PI));
            int g = (int) (64 + 64 * Mth.sin(colorPhase * Mth.TWO_PI + 1));
            int b = (int) (180 + 75 * Mth.sin(colorPhase * Mth.TWO_PI + 2));
            int color = 0xAA000000 | (r << 16) | (g << 8) | b;

            drawCircle(graphics, px, py, 2, color);
        }

        drawCircleRing(graphics, cx, cy, eventHorizonRadius + 3, 2, 0x60FFFFFF);

        for (int r = eventHorizonRadius; r > eventHorizonRadius - 10 && r > 0; r--) {
            int alpha = (int) (40 * (1f - (float) (eventHorizonRadius - r) / 10f));
            drawCircle(graphics, cx, cy, r, (alpha << 24) | 0x6040A0);
        }
    }

    private void drawDeathCore(GuiGraphics graphics, int cx, int cy, int radius, Stage stage) {
        boolean graceful = stage == Stage.DEATH_GRACEFUL;

        float pulse;
        if (graceful) {
            pulse = 0.3f + 0.2f * Mth.sin(pulsePhase * 0.3f);
        } else {
            pulse = 0.5f + 0.3f * Mth.sin(pulsePhase * 3f) + 0.2f * Mth.sin(pulsePhase * 7f + 1.3f) +
                    0.1f * Mth.sin(pulsePhase * 11f + 2.7f);
            pulse = Mth.clamp(pulse, 0.2f, 1.2f);
        }

        int coreRadius = (int) (radius * 0.5f * pulse);

        int[] colors = graceful ? new int[] { 0x301010, 0x502020, 0x703030, 0x904040 } :
                new int[] { 0x660000, 0xAA0000, 0xDD2200, 0xFF4400 };

        for (int layer = 0; layer < colors.length; layer++) {
            int layerRadius = coreRadius - layer * 3;
            if (layerRadius > 0) {
                int alpha = graceful ? (80 + layer * 20) : (150 + layer * 25);
                alpha = Math.min(255, alpha);
                drawCircle(graphics, cx, cy, layerRadius, (alpha << 24) | colors[layer]);
            }
        }

        if (!graceful && Math.random() < 0.1) {
            int flickerRadius = coreRadius + (int) (Math.random() * 10);
            drawCircle(graphics, cx, cy, flickerRadius, 0x40FF0000);
        }
    }

    private void drawSolarFlares(GuiGraphics graphics, int cx, int cy, float baseRadius, int color) {
        int flareCount = 5;
        for (int i = 0; i < flareCount; i++) {
            float angle = animPhase * 0.5f + i * Mth.TWO_PI / flareCount;
            float flareLength = 8 + 5 * Mth.sin(animPhase * 2 + i * 1.3f);
            float dist = baseRadius + flareLength;

            int px = cx + (int) (Mth.cos(angle) * dist);
            int py = cy + (int) (Mth.sin(angle) * dist);

            int alpha = (int) (80 + 40 * Mth.sin(animPhase * 3 + i));
            drawCircle(graphics, px, py, 3, (alpha << 24) | color);
        }
    }

    private void drawCircle(GuiGraphics graphics, int cx, int cy, int radius, int color) {
        if (radius <= 0) return;
        for (int y = -radius; y <= radius; y++) {
            int halfWidth = (int) Math.sqrt(radius * radius - y * y);
            graphics.fill(cx - halfWidth, cy + y, cx + halfWidth + 1, cy + y + 1, color);
        }
    }

    private void drawCircleRing(GuiGraphics graphics, int cx, int cy, int radius, int thickness, int color) {
        for (int t = 0; t < thickness; t++) {
            int r = radius - t;
            if (r <= 0) continue;
            for (int angle = 0; angle < 360; angle += 3) {
                float rad = angle * Mth.DEG_TO_RAD;
                int px = cx + (int) (Mth.cos(rad) * r);
                int py = cy + (int) (Mth.sin(rad) * r);
                graphics.fill(px, py, px + 1, py + 1, color);
            }
        }
    }

    private void drawStageLabel(GuiGraphics graphics, int cx, Stage stage) {
        String label = switch (stage) {
            case EMPTY -> "DORMANT";
            case GROWING -> "IGNITING";
            case STAR -> "MAIN SEQUENCE";
            case SUPERSTAR -> "RED GIANT";
            case BLACK_HOLE -> "SINGULARITY";
            case DEATH -> "UNSTABLE";
            case DEATH_GRACEFUL -> "FADING";
        };

        var font = Minecraft.getInstance().font;
        int labelWidth = font.width(label);
        int labelX = cx - labelWidth / 2;
        int labelY = getPosition().y + getSize().height - 12;

        int textColor = getStageTextColor(stage);
        graphics.drawString(font, label, labelX, labelY, textColor, false);
    }

    private int getStageTextColor(Stage stage) {
        int customColor = getCustomColor();
        if (customColor != -1 && (stage == Stage.STAR || stage == Stage.SUPERSTAR)) {
            return 0xFF000000 | customColor;
        }

        return switch (stage) {
            case EMPTY -> 0xFF606080;
            case GROWING -> 0xFF8090FF;
            case STAR -> 0xFFFFCC44;
            case SUPERSTAR -> 0xFFFF8844;
            case BLACK_HOLE -> 0xFFAA66FF;
            case DEATH -> 0xFFFF4444;
            case DEATH_GRACEFUL -> 0xFF884444;
        };
    }

    private int[] generateColorGradient(int baseColor) {
        int[] gradient = new int[5];
        float[] hsb = rgbToHsb(baseColor);

        for (int i = 0; i < 5; i++) {
            float brightness = 0.3f + (i * 0.175f);
            float saturation = Math.max(0.2f, hsb[1] - (i * 0.1f));
            gradient[i] = hsbToRgb(hsb[0], saturation, Math.min(1f, brightness));
        }

        return gradient;
    }

    private int blendTowardsWhite(int color, float factor) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        r = (int) (r + (255 - r) * factor);
        g = (int) (g + (255 - g) * factor);
        b = (int) (b + (255 - b) * factor);

        return (r << 16) | (g << 8) | b;
    }

    private int darken(int color, float factor) {
        int r = (int) (((color >> 16) & 0xFF) * factor);
        int g = (int) (((color >> 8) & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);

        return (r << 16) | (g << 8) | b;
    }

    private int shiftHue(int color, float shift) {
        float[] hsb = rgbToHsb(color);
        hsb[0] = (hsb[0] + shift) % 1f;
        if (hsb[0] < 0) hsb[0] += 1f;
        return hsbToRgb(hsb[0], hsb[1], hsb[2]);
    }

    private static float[] rgbToHsb(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        float[] hsb = new float[3];
        java.awt.Color.RGBtoHSB(r, g, b, hsb);
        return hsb;
    }

    private static int hsbToRgb(float h, float s, float b) {
        return java.awt.Color.HSBtoRGB(h, s, b) & 0xFFFFFF;
    }
}
