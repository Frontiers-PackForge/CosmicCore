package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine.Stage;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

public class StarfieldBackgroundWidget extends Widget {

    private final Supplier<Stage> stageSupplier;
    private final List<BackgroundStar> stars = new ArrayList<>();
    private final List<Nebula> nebulae = new ArrayList<>();
    private final Random random = new Random(42);

    private float driftPhase = 0f;
    private float nebulaPhase = 0f;

    private static class BackgroundStar {

        float x, y;
        float baseX, baseY;
        float size;
        float twinkleSpeed;
        float twinkleOffset;
        int color;
        float depth;
    }

    private static class Nebula {

        float x, y;
        float radius;
        int color;
        float pulseSpeed;
        float pulseOffset;
    }

    public StarfieldBackgroundWidget(int x, int y, int width, int height, Supplier<Stage> stageSupplier) {
        super(x, y, width, height);
        this.stageSupplier = stageSupplier;
        initStars(width, height);
        initNebulae(width, height);
    }

    private void initStars(int w, int h) {
        int starCount = 80;
        for (int i = 0; i < starCount; i++) {
            BackgroundStar star = new BackgroundStar();
            star.baseX = random.nextFloat() * w;
            star.baseY = random.nextFloat() * h;
            star.x = star.baseX;
            star.y = star.baseY;
            star.size = 0.5f + random.nextFloat() * 1.5f;
            star.twinkleSpeed = 0.02f + random.nextFloat() * 0.05f;
            star.twinkleOffset = random.nextFloat() * Mth.TWO_PI;
            star.depth = 0.3f + random.nextFloat() * 0.7f;

            float colorRand = random.nextFloat();
            if (colorRand < 0.6f) {
                star.color = 0xFFFFFF;
            } else if (colorRand < 0.75f) {
                star.color = 0xFFDDAA;
            } else if (colorRand < 0.85f) {
                star.color = 0xAADDFF;
            } else if (colorRand < 0.95f) {
                star.color = 0xFFAAAA;
            } else {
                star.color = 0xAAFFAA;
            }

            stars.add(star);
        }
    }

    private void initNebulae(int w, int h) {
        int nebulaCount = 4;
        int[] nebulaColors = { 0x4020A0, 0xA02040, 0x204080, 0x802060 };

        for (int i = 0; i < nebulaCount; i++) {
            Nebula nebula = new Nebula();
            nebula.x = random.nextFloat() * w;
            nebula.y = random.nextFloat() * h;
            nebula.radius = 30 + random.nextFloat() * 50;
            nebula.color = nebulaColors[i % nebulaColors.length];
            nebula.pulseSpeed = 0.01f + random.nextFloat() * 0.02f;
            nebula.pulseOffset = random.nextFloat() * Mth.TWO_PI;
            nebulae.add(nebula);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();

        Stage stage = stageSupplier.get();
        float driftSpeed = getDriftSpeed(stage);
        driftPhase += driftSpeed;
        nebulaPhase += 0.02f;

        float driftX = Mth.sin(driftPhase * 0.3f) * 2f;
        float driftY = Mth.cos(driftPhase * 0.2f) * 1.5f;

        for (BackgroundStar star : stars) {
            star.x = star.baseX + driftX * star.depth;
            star.y = star.baseY + driftY * star.depth;
        }
    }

    private float getDriftSpeed(Stage stage) {
        return switch (stage) {
            case EMPTY -> 0.01f;
            case GROWING -> 0.02f;
            case STAR -> 0.015f;
            case SUPERSTAR -> 0.03f;
            case BLACK_HOLE -> 0.05f;
            case DEATH -> 0.08f;
            case DEATH_GRACEFUL -> 0.005f;
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

        drawDeepSpaceGradient(graphics, x, y, w, h, stage);
        drawNebulae(graphics, x, y, w, h, stage);
        drawStars(graphics, x, y, w, h, stage);

        if (stage == Stage.BLACK_HOLE) {
            drawGravitationalDistortion(graphics, x, y, w, h);
        }
    }

    private void drawDeepSpaceGradient(GuiGraphics graphics, int x, int y, int w, int h, Stage stage) {
        int topColor, bottomColor;

        switch (stage) {
            case EMPTY -> {
                topColor = 0xFF08080C;
                bottomColor = 0xFF040406;
            }
            case GROWING -> {
                topColor = 0xFF0A0A14;
                bottomColor = 0xFF060610;
            }
            case STAR -> {
                topColor = 0xFF100808;
                bottomColor = 0xFF080404;
            }
            case SUPERSTAR -> {
                topColor = 0xFF140808;
                bottomColor = 0xFF0A0404;
            }
            case BLACK_HOLE -> {
                topColor = 0xFF0C0410;
                bottomColor = 0xFF040208;
            }
            case DEATH -> {
                topColor = 0xFF140404;
                bottomColor = 0xFF0A0202;
            }
            default -> {
                topColor = 0xFF0A0808;
                bottomColor = 0xFF050404;
            }
        }

        for (int row = 0; row < h; row++) {
            float progress = (float) row / h;
            int color = lerpColor(topColor, bottomColor, progress);
            graphics.fill(x, y + row, x + w, y + row + 1, color);
        }
    }

    private void drawNebulae(GuiGraphics graphics, int x, int y, int w, int h, Stage stage) {
        float intensity = switch (stage) {
            case EMPTY -> 0.3f;
            case GROWING -> 0.5f;
            case STAR -> 0.6f;
            case SUPERSTAR -> 0.8f;
            case BLACK_HOLE -> 1.0f;
            case DEATH -> 0.4f;
            case DEATH_GRACEFUL -> 0.2f;
        };

        for (Nebula nebula : nebulae) {
            float pulse = Mth.sin(nebulaPhase + nebula.pulseOffset) * 0.3f + 0.7f;
            float radius = nebula.radius * pulse;

            int layers = 8;
            for (int layer = layers; layer > 0; layer--) {
                float layerProgress = (float) layer / layers;
                float layerRadius = radius * layerProgress;

                int alpha = (int) (0x08 * intensity * (1f - layerProgress * 0.7f));
                int color = (alpha << 24) | (nebula.color & 0x00FFFFFF);

                int nx = x + (int) nebula.x;
                int ny = y + (int) nebula.y;
                int r = (int) layerRadius;

                for (int py = -r; py <= r; py++) {
                    int halfWidth = (int) Math.sqrt(r * r - py * py);
                    int drawY = ny + py;
                    if (drawY >= y && drawY < y + h) {
                        int x1 = Math.max(x, nx - halfWidth);
                        int x2 = Math.min(x + w, nx + halfWidth);
                        if (x1 < x2) {
                            graphics.fill(x1, drawY, x2, drawY + 1, color);
                        }
                    }
                }
            }
        }
    }

    private void drawStars(GuiGraphics graphics, int x, int y, int w, int h, Stage stage) {
        float baseAlpha = switch (stage) {
            case EMPTY -> 0.4f;
            case GROWING -> 0.6f;
            case STAR -> 0.8f;
            case SUPERSTAR -> 0.7f;
            case BLACK_HOLE -> 0.5f;
            case DEATH -> 0.3f;
            case DEATH_GRACEFUL -> 0.5f;
        };

        for (BackgroundStar star : stars) {
            float twinkle = Mth.sin(driftPhase * star.twinkleSpeed * 60f + star.twinkleOffset);
            float brightness = 0.6f + 0.4f * twinkle;
            int alpha = (int) (0xFF * baseAlpha * brightness);

            int starX = x + (int) star.x;
            int starY = y + (int) star.y;

            if (starX < x || starX >= x + w || starY < y || starY >= y + h) continue;

            int color = (alpha << 24) | (star.color & 0x00FFFFFF);

            if (star.size > 1.2f) {
                graphics.fill(starX - 1, starY, starX + 2, starY + 1, color);
                graphics.fill(starX, starY - 1, starX + 1, starY + 2, color);
            } else {
                graphics.fill(starX, starY, starX + 1, starY + 1, color);
            }

            if (star.size > 1.5f && brightness > 0.8f) {
                int glowAlpha = (int) (alpha * 0.3f);
                int glowColor = (glowAlpha << 24) | (star.color & 0x00FFFFFF);
                graphics.fill(starX - 1, starY - 1, starX + 2, starY + 2, glowColor);
            }
        }
    }

    private void drawGravitationalDistortion(GuiGraphics graphics, int x, int y, int w, int h) {
        int cx = x + w / 2;
        int cy = y + h / 2;

        for (int ring = 0; ring < 5; ring++) {
            float ringPhase = driftPhase * 0.5f + ring * 0.5f;
            float ringRadius = 30 + ring * 20 + Mth.sin(ringPhase) * 5;

            int alpha = 0x15 - ring * 0x03;
            int color = (alpha << 24) | 0x8040FF;

            int r = (int) ringRadius;
            for (int angle = 0; angle < 360; angle += 4) {
                float rad = angle * Mth.DEG_TO_RAD;
                int px = cx + (int) (Mth.cos(rad) * r);
                int py = cy + (int) (Mth.sin(rad) * r * 0.4f);

                if (px >= x && px < x + w && py >= y && py < y + h) {
                    graphics.fill(px, py, px + 1, py + 1, color);
                }
            }
        }
    }

    private int lerpColor(int c1, int c2, float t) {
        int a1 = (c1 >> 24) & 0xFF, a2 = (c2 >> 24) & 0xFF;
        int r1 = (c1 >> 16) & 0xFF, r2 = (c2 >> 16) & 0xFF;
        int g1 = (c1 >> 8) & 0xFF, g2 = (c2 >> 8) & 0xFF;
        int b1 = c1 & 0xFF, b2 = c2 & 0xFF;

        int a = (int) Mth.lerp(t, a1, a2);
        int r = (int) Mth.lerp(t, r1, r2);
        int g = (int) Mth.lerp(t, g1, g2);
        int b = (int) Mth.lerp(t, b1, b2);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
