package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine.Stage;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class StellarCoreWidget extends Widget {

    private final Supplier<Stage> stageSupplier;

    private float animPhase = 0f;
    private float pulsePhase = 0f;
    private float transitionProgress = 1f;
    private Stage previousStage = Stage.EMPTY;
    private Stage targetStage = Stage.EMPTY;

    public StellarCoreWidget(int x, int y, int size, Supplier<Stage> stageSupplier) {
        super(x, y, size, size);
        this.stageSupplier = stageSupplier;
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

        int cx = getPosition().x + getSize().width / 2;
        int cy = getPosition().y + getSize().height / 2;
        int maxRadius = getSize().width / 2 - 5;

        Stage stage = stageSupplier.get();

        // Background void effect
        drawVoidBackground(graphics, cx, cy, maxRadius);

        // Stage-specific rendering
        switch (stage) {
            case EMPTY -> drawEmptyCore(graphics, cx, cy, maxRadius);
            case GROWING -> drawGrowingCore(graphics, cx, cy, maxRadius);
            case STAR -> drawStarCore(graphics, cx, cy, maxRadius);
            case SUPERSTAR -> drawSuperstarCore(graphics, cx, cy, maxRadius);
            case BLACK_HOLE -> drawBlackHoleCore(graphics, cx, cy, maxRadius);
            case DEATH, DEATH_GRACEFUL -> drawDeathCore(graphics, cx, cy, maxRadius, stage);
        }

        // Stage label at bottom
        drawStageLabel(graphics, cx, stage);
    }

    private void drawVoidBackground(GuiGraphics graphics, int cx, int cy, int radius) {
        // Dark void circle background
        for (int r = radius; r > 0; r -= 3) {
            float progress = (float) r / radius;
            int alpha = (int) (30 * progress);
            int color = (alpha << 24) | 0x101020;
            drawCircle(graphics, cx, cy, r, color);
        }
    }

    private void drawEmptyCore(GuiGraphics graphics, int cx, int cy, int radius) {
        // Dim pulsing outline suggesting potential
        float pulse = 0.5f + 0.3f * Mth.sin(pulsePhase);
        int alpha = (int) (pulse * 60);

        // Outer ring
        drawCircleRing(graphics, cx, cy, radius - 5, 2, (alpha << 24) | 0x404060);

        // Inner dim glow
        int innerRadius = radius / 3;
        for (int r = innerRadius; r > 0; r -= 2) {
            float glowProgress = (float) r / innerRadius;
            int glowAlpha = (int) (20 * glowProgress * pulse);
            drawCircle(graphics, cx, cy, r, (glowAlpha << 24) | 0x303050);
        }

        // Center point
        drawCircle(graphics, cx, cy, 3, (int)(pulse * 100) << 24 | 0x505080);
    }

    private void drawGrowingCore(GuiGraphics graphics, int cx, int cy, int radius) {
        // Growing star with gathering particles
        float pulse = 0.8f + 0.2f * Mth.sin(pulsePhase * 1.5f);
        float grow = 0.3f + 0.4f * (Mth.sin(animPhase * 0.5f) * 0.5f + 0.5f);

        int coreRadius = (int) (radius * grow * pulse);

        // Outer gathering energy swirls
        for (int i = 0; i < 8; i++) {
            float angle = animPhase * 2f + i * Mth.PI / 4f;
            float dist = radius * 0.8f * (0.5f + 0.5f * Mth.sin(animPhase + i));
            int px = cx + (int) (Mth.cos(angle) * dist);
            int py = cy + (int) (Mth.sin(angle) * dist);
            int pAlpha = (int) (100 * (1f - dist / (radius * 0.8f)));
            drawCircle(graphics, px, py, 2, (pAlpha << 24) | 0x6090FF);
        }

        // Core glow layers
        int[] colors = {0x2040A0, 0x4060C0, 0x6080E0, 0x80A0FF};
        for (int layer = 0; layer < colors.length; layer++) {
            int layerRadius = coreRadius - layer * 3;
            if (layerRadius > 0) {
                int alpha = 60 + layer * 30;
                drawCircle(graphics, cx, cy, layerRadius, (alpha << 24) | colors[layer]);
            }
        }

        // Bright center
        drawCircle(graphics, cx, cy, Math.max(3, coreRadius / 4), 0xDDFFFFFF);
    }

    private void drawStarCore(GuiGraphics graphics, int cx, int cy, int radius) {
        float pulse = 0.95f + 0.05f * Mth.sin(pulsePhase);
        int coreRadius = (int) (radius * 0.7f * pulse);

        // Corona/outer glow
        for (int r = coreRadius + 15; r > coreRadius; r -= 2) {
            float glowProgress = (float) (r - coreRadius) / 15f;
            int alpha = (int) ((1f - glowProgress) * 60);
            drawCircle(graphics, cx, cy, r, (alpha << 24) | 0xFFAA44);
        }

        // Main star body - golden yellow gradient
        int[] colors = {0x804000, 0xCC6600, 0xFF9900, 0xFFCC44, 0xFFEE88};
        for (int layer = 0; layer < colors.length; layer++) {
            int layerRadius = coreRadius - layer * (coreRadius / colors.length);
            if (layerRadius > 0) {
                int alpha = 180 + layer * 15;
                alpha = Math.min(255, alpha);
                drawCircle(graphics, cx, cy, layerRadius, (alpha << 24) | colors[layer]);
            }
        }

        // Hot white center
        int hotCenterRadius = coreRadius / 4;
        drawCircle(graphics, cx, cy, hotCenterRadius, 0xEEFFFFFF);

        // Solar flare particles
        drawSolarFlares(graphics, cx, cy, coreRadius, 0xFFAA44);
    }

    private void drawSuperstarCore(GuiGraphics graphics, int cx, int cy, int radius) {
        float pulse = 0.9f + 0.1f * Mth.sin(pulsePhase * 0.7f);
        int coreRadius = (int) (radius * 0.85f * pulse);

        // Massive corona
        for (int r = coreRadius + 20; r > coreRadius; r -= 2) {
            float glowProgress = (float) (r - coreRadius) / 20f;
            int alpha = (int) ((1f - glowProgress) * 80);
            drawCircle(graphics, cx, cy, r, (alpha << 24) | 0xFF6622);
        }

        // Superstar body - orange/red gradient
        int[] colors = {0x661100, 0xAA3300, 0xDD5500, 0xFF7722, 0xFFAA44};
        for (int layer = 0; layer < colors.length; layer++) {
            int layerRadius = coreRadius - layer * (coreRadius / colors.length);
            if (layerRadius > 0) {
                int alpha = 200 + layer * 11;
                alpha = Math.min(255, alpha);
                drawCircle(graphics, cx, cy, layerRadius, (alpha << 24) | colors[layer]);
            }
        }

        // Bright core
        drawCircle(graphics, cx, cy, coreRadius / 3, 0xFFFFEECC);

        // Intense flares
        drawSolarFlares(graphics, cx, cy, coreRadius, 0xFF5500);
        drawSolarFlares(graphics, cx, cy, coreRadius * 0.8f, 0xFFAA22);
    }

    private void drawBlackHoleCore(GuiGraphics graphics, int cx, int cy, int radius) {
        // Event horizon - pure black center
        int eventHorizonRadius = (int) (radius * 0.3f);
        drawCircle(graphics, cx, cy, eventHorizonRadius, 0xFF000000);

        // Accretion disk
        float diskPulse = 0.9f + 0.1f * Mth.sin(pulsePhase * 0.5f);
        for (int i = 0; i < 360; i += 5) {
            float angle = Mth.DEG_TO_RAD * i + animPhase;
            float diskRadius = radius * 0.7f * diskPulse;
            float variance = 0.1f * Mth.sin(angle * 3 + animPhase * 2);
            diskRadius *= (1f + variance);

            int px = cx + (int) (Mth.cos(angle) * diskRadius);
            int py = cy + (int) (Mth.sin(angle) * diskRadius * 0.3f); // Flattened

            // Color varies around disk
            float colorPhase = (i / 360f + animPhase * 0.1f) % 1f;
            int r = (int) (128 + 127 * Mth.sin(colorPhase * Mth.TWO_PI));
            int g = (int) (64 + 64 * Mth.sin(colorPhase * Mth.TWO_PI + 1));
            int b = (int) (180 + 75 * Mth.sin(colorPhase * Mth.TWO_PI + 2));
            int color = 0xAA000000 | (r << 16) | (g << 8) | b;

            drawCircle(graphics, px, py, 2, color);
        }

        // Gravitational lensing ring
        drawCircleRing(graphics, cx, cy, eventHorizonRadius + 3, 2, 0x60FFFFFF);

        // Hawking radiation glow
        for (int r = eventHorizonRadius; r > eventHorizonRadius - 10 && r > 0; r--) {
            int alpha = (int) (40 * (1f - (float)(eventHorizonRadius - r) / 10f));
            drawCircle(graphics, cx, cy, r, (alpha << 24) | 0x6040A0);
        }
    }

    private void drawDeathCore(GuiGraphics graphics, int cx, int cy, int radius, Stage stage) {
        boolean graceful = stage == Stage.DEATH_GRACEFUL;

        // Erratic pulsing for death, slow fade for graceful
        float pulse;
        if (graceful) {
            pulse = 0.3f + 0.2f * Mth.sin(pulsePhase * 0.3f);
        } else {
            // Erratic - combine multiple frequencies
            pulse = 0.5f + 0.3f * Mth.sin(pulsePhase * 3f)
                  + 0.2f * Mth.sin(pulsePhase * 7f + 1.3f)
                  + 0.1f * Mth.sin(pulsePhase * 11f + 2.7f);
            pulse = Mth.clamp(pulse, 0.2f, 1.2f);
        }

        int coreRadius = (int) (radius * 0.5f * pulse);

        // Unstable red core
        int[] colors = graceful
            ? new int[]{0x301010, 0x502020, 0x703030, 0x904040}
            : new int[]{0x660000, 0xAA0000, 0xDD2200, 0xFF4400};

        for (int layer = 0; layer < colors.length; layer++) {
            int layerRadius = coreRadius - layer * 3;
            if (layerRadius > 0) {
                int alpha = graceful ? (80 + layer * 20) : (150 + layer * 25);
                alpha = Math.min(255, alpha);
                drawCircle(graphics, cx, cy, layerRadius, (alpha << 24) | colors[layer]);
            }
        }

        // Warning flickers for non-graceful death
        if (!graceful && Math.random() < 0.1) {
            int flickerRadius = coreRadius + (int)(Math.random() * 10);
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
}
