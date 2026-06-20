package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine.Stage;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

public class OrbitalRingsWidget extends Widget {

    private final Supplier<Stage> stageSupplier;
    private final int centerX;
    private final int centerY;

    private float rotationPhase = 0f;
    private float wobblePhase = 0f;
    private float pulsePhase = 0f;

    public OrbitalRingsWidget(int x, int y, int width, int height, Supplier<Stage> stageSupplier) {
        super(x, y, width, height);
        this.stageSupplier = stageSupplier;
        this.centerX = width / 2;
        this.centerY = height / 2;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();

        Stage stage = stageSupplier.get();
        float rotSpeed = getRotationSpeed(stage);

        rotationPhase += rotSpeed;
        wobblePhase += rotSpeed * 0.7f;
        pulsePhase += 0.08f;
    }

    private float getRotationSpeed(Stage stage) {
        return switch (stage) {
            case EMPTY -> 0.005f;
            case GROWING -> 0.02f;
            case STAR -> 0.015f;
            case SUPERSTAR -> 0.03f;
            case BLACK_HOLE -> 0.06f;
            case DEATH -> 0.1f;
            case DEATH_GRACEFUL -> 0.003f;
        };
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);

        int x = getPosition().x;
        int y = getPosition().y;
        int cx = x + centerX;
        int cy = y + centerY;

        Stage stage = stageSupplier.get();
        int baseColor = getStageColor(stage);

        int ringCount = getRingCount(stage);
        for (int ring = 0; ring < ringCount; ring++) {
            drawOrbitalRing(graphics, cx, cy, ring, baseColor, stage);
        }

        if (stage != Stage.EMPTY && stage != Stage.DEATH_GRACEFUL) {
            drawOrbitalParticles(graphics, cx, cy, baseColor, stage);
        }

        if (stage == Stage.BLACK_HOLE) {
            drawAccretionDisk(graphics, cx, cy);
        }
    }

    private int getRingCount(Stage stage) {
        return switch (stage) {
            case EMPTY -> 1;
            case GROWING -> 2;
            case STAR -> 3;
            case SUPERSTAR -> 4;
            case BLACK_HOLE -> 5;
            case DEATH -> 3;
            case DEATH_GRACEFUL -> 2;
        };
    }

    private void drawOrbitalRing(GuiGraphics graphics, int cx, int cy, int ringIndex, int baseColor, Stage stage) {
        float baseRadius = 25 + ringIndex * 15;
        float tilt = 0.3f + ringIndex * 0.1f;
        float ringRotation = rotationPhase * (1f + ringIndex * 0.3f) + ringIndex * 1.2f;
        float wobble = Mth.sin(wobblePhase + ringIndex * 0.8f) * 0.05f;

        float pulse = Mth.sin(pulsePhase + ringIndex * 0.5f) * 0.2f + 0.8f;
        int alpha = (int) (0x40 * pulse);

        if (stage == Stage.DEATH) {
            alpha = (int) (alpha * (0.5f + Math.random() * 0.5f));
            baseRadius += (float) (Math.random() - 0.5) * 5;
        }

        int color = (alpha << 24) | (baseColor & 0x00FFFFFF);

        int segments = 60;
        for (int i = 0; i < segments; i++) {
            float angle = ringRotation + (i * Mth.TWO_PI / segments);
            float nextAngle = ringRotation + ((i + 1) * Mth.TWO_PI / segments);

            float segmentBrightness = (Mth.sin(angle * 3 + pulsePhase) + 1f) * 0.3f + 0.4f;
            int segmentAlpha = (int) (alpha * segmentBrightness);
            int segmentColor = (segmentAlpha << 24) | (baseColor & 0x00FFFFFF);

            float x1 = cx + Mth.cos(angle) * baseRadius;
            float y1 = cy + Mth.sin(angle) * baseRadius * (tilt + wobble);
            float x2 = cx + Mth.cos(nextAngle) * baseRadius;
            float y2 = cy + Mth.sin(nextAngle) * baseRadius * (tilt + wobble);

            drawLine(graphics, (int) x1, (int) y1, (int) x2, (int) y2, segmentColor);
        }

        if (stage == Stage.STAR || stage == Stage.SUPERSTAR) {
            int glowAlpha = alpha / 3;
            int glowColor = (glowAlpha << 24) | (baseColor & 0x00FFFFFF);
            float glowRadius = baseRadius + 2;

            for (int i = 0; i < segments; i += 2) {
                float angle = ringRotation + (i * Mth.TWO_PI / segments);
                float gx = cx + Mth.cos(angle) * glowRadius;
                float gy = cy + Mth.sin(angle) * glowRadius * tilt;
                graphics.fill((int) gx - 1, (int) gy - 1, (int) gx + 2, (int) gy + 2, glowColor);
            }
        }
    }

    private void drawOrbitalParticles(GuiGraphics graphics, int cx, int cy, int baseColor, Stage stage) {
        int particleCount = switch (stage) {
            case GROWING -> 4;
            case STAR -> 6;
            case SUPERSTAR -> 10;
            case BLACK_HOLE -> 15;
            case DEATH -> 8;
            default -> 2;
        };

        for (int i = 0; i < particleCount; i++) {
            float particleOrbit = 20 + (i * 37) % 50;
            float particleSpeed = 1f + (i % 3) * 0.5f;
            float particleAngle = rotationPhase * particleSpeed + i * 0.9f;
            float particleTilt = 0.2f + (i % 4) * 0.1f;

            float px = cx + Mth.cos(particleAngle) * particleOrbit;
            float py = cy + Mth.sin(particleAngle) * particleOrbit * particleTilt;

            float brightness = Mth.sin(pulsePhase + i * 0.7f) * 0.4f + 0.6f;
            int alpha = (int) (0xC0 * brightness);

            int particleColor;
            if (stage == Stage.BLACK_HOLE) {
                float hue = (particleAngle * 0.1f) % 1f;
                particleColor = (alpha << 24) | hslToRgb(hue, 0.8f, 0.6f);
            } else {
                particleColor = (alpha << 24) | (baseColor & 0x00FFFFFF);
            }

            int size = 1 + (i % 2);
            graphics.fill((int) px - size, (int) py - size, (int) px + size + 1, (int) py + size + 1, particleColor);

            int trailLength = 3;
            for (int t = 1; t <= trailLength; t++) {
                float trailAngle = particleAngle - t * 0.15f;
                float tx = cx + Mth.cos(trailAngle) * particleOrbit;
                float ty = cy + Mth.sin(trailAngle) * particleOrbit * particleTilt;
                int trailAlpha = alpha / (t + 1);
                int trailColor = (trailAlpha << 24) | (baseColor & 0x00FFFFFF);
                graphics.fill((int) tx, (int) ty, (int) tx + 1, (int) ty + 1, trailColor);
            }
        }
    }

    private void drawAccretionDisk(GuiGraphics graphics, int cx, int cy) {
        float diskRadius = 55;
        float innerRadius = 20;

        int diskSegments = 120;
        for (int i = 0; i < diskSegments; i++) {
            float angle = rotationPhase * 0.3f + i * Mth.TWO_PI / diskSegments;
            float radiusVariation = Mth.sin(angle * 8 + pulsePhase * 2) * 5;
            float currentRadius = diskRadius + radiusVariation;

            float hue = ((angle + rotationPhase) * 0.15f) % 1f;
            float brightness = 0.3f + Mth.sin(angle * 4 + pulsePhase) * 0.2f;

            for (float r = innerRadius; r < currentRadius; r += 3) {
                float radialBrightness = 1f - (r - innerRadius) / (currentRadius - innerRadius);
                int alpha = (int) (0x30 * radialBrightness * brightness);
                int color = (alpha << 24) | hslToRgb(hue, 0.7f, 0.5f + radialBrightness * 0.3f);

                float dx = cx + Mth.cos(angle) * r;
                float dy = cy + Mth.sin(angle) * r * 0.25f;
                graphics.fill((int) dx, (int) dy, (int) dx + 2, (int) dy + 1, color);
            }
        }
    }

    private void drawLine(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            graphics.fill(x1, y1, x1 + 1, y1 + 1, color);

            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

    private int hslToRgb(float h, float s, float l) {
        float c = (1 - Math.abs(2 * l - 1)) * s;
        float x = c * (1 - Math.abs((h * 6) % 2 - 1));
        float m = l - c / 2;

        float r, g, b;
        if (h < 1f / 6) {
            r = c;
            g = x;
            b = 0;
        } else if (h < 2f / 6) {
            r = x;
            g = c;
            b = 0;
        } else if (h < 3f / 6) {
            r = 0;
            g = c;
            b = x;
        } else if (h < 4f / 6) {
            r = 0;
            g = x;
            b = c;
        } else if (h < 5f / 6) {
            r = x;
            g = 0;
            b = c;
        } else {
            r = c;
            g = 0;
            b = x;
        }

        int ri = (int) ((r + m) * 255);
        int gi = (int) ((g + m) * 255);
        int bi = (int) ((b + m) * 255);

        return (ri << 16) | (gi << 8) | bi;
    }

    private int getStageColor(Stage stage) {
        return switch (stage) {
            case EMPTY -> 0x506080;
            case GROWING -> 0x80A0FF;
            case STAR -> 0xFFDD66;
            case SUPERSTAR -> 0xFF9944;
            case BLACK_HOLE -> 0xBB66FF;
            case DEATH -> 0xFF4040;
            case DEATH_GRACEFUL -> 0x806060;
        };
    }
}
