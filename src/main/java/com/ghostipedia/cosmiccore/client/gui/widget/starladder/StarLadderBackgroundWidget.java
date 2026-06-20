package com.ghostipedia.cosmiccore.client.gui.widget.starladder;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

public class StarLadderBackgroundWidget extends Widget {

    private final List<BackgroundStar> stars = new ArrayList<>();
    private final List<GridLine> gridLines = new ArrayList<>();
    private final Random random = new Random(12345);

    private float driftPhase = 0f;
    private float gridPulse = 0f;

    private static class BackgroundStar {

        float x, y;
        float baseX, baseY;
        float size;
        float twinkleSpeed;
        float twinkleOffset;
        int color;
        float depth;
    }

    private static class GridLine {

        float pos;
        boolean horizontal;
        float pulseOffset;
    }

    public StarLadderBackgroundWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
        initStars(width, height);
        initGrid(width, height);
    }

    private void initStars(int w, int h) {
        int starCount = 50;
        for (int i = 0; i < starCount; i++) {
            BackgroundStar star = new BackgroundStar();
            star.baseX = random.nextFloat() * w;
            star.baseY = random.nextFloat() * h;
            star.x = star.baseX;
            star.y = star.baseY;
            star.size = 0.5f + random.nextFloat() * 1.2f;
            star.twinkleSpeed = 0.015f + random.nextFloat() * 0.04f;
            star.twinkleOffset = random.nextFloat() * Mth.TWO_PI;
            star.depth = 0.3f + random.nextFloat() * 0.7f;

            float colorRand = random.nextFloat();
            if (colorRand < 0.5f) {
                star.color = 0xAABBFF;
            } else if (colorRand < 0.7f) {
                star.color = 0xFFFFFF;
            } else if (colorRand < 0.85f) {
                star.color = 0x99DDFF;
            } else {
                star.color = 0xDDAAFF;
            }
            stars.add(star);
        }
    }

    private void initGrid(int w, int h) {
        int gridSpacing = 20;
        for (int x = gridSpacing; x < w; x += gridSpacing) {
            GridLine line = new GridLine();
            line.pos = x;
            line.horizontal = false;
            line.pulseOffset = random.nextFloat() * Mth.TWO_PI;
            gridLines.add(line);
        }
        for (int y = gridSpacing; y < h; y += gridSpacing) {
            GridLine line = new GridLine();
            line.pos = y;
            line.horizontal = true;
            line.pulseOffset = random.nextFloat() * Mth.TWO_PI;
            gridLines.add(line);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        driftPhase += 0.012f;
        gridPulse += 0.03f;

        float driftX = Mth.sin(driftPhase * 0.3f) * 1.5f;
        float driftY = Mth.cos(driftPhase * 0.2f) * 1f;

        for (BackgroundStar star : stars) {
            star.x = star.baseX + driftX * star.depth;
            star.y = star.baseY + driftY * star.depth;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);

        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        drawSpaceGradient(graphics, x, y, w, h);
        drawGrid(graphics, x, y, w, h);
        drawStars(graphics, x, y, w, h);
    }

    private void drawSpaceGradient(GuiGraphics graphics, int x, int y, int w, int h) {
        int topColor = 0xFF060812;
        int bottomColor = 0xFF020408;

        for (int row = 0; row < h; row++) {
            float progress = (float) row / h;
            int color = lerpColor(topColor, bottomColor, progress);
            graphics.fill(x, y + row, x + w, y + row + 1, color);
        }
    }

    private void drawGrid(GuiGraphics graphics, int x, int y, int w, int h) {
        for (GridLine line : gridLines) {
            float pulse = Mth.sin(gridPulse + line.pulseOffset) * 0.3f + 0.7f;
            int alpha = (int) (0x15 * pulse);
            int color = (alpha << 24) | 0x4080FF;

            if (line.horizontal) {
                int ly = y + (int) line.pos;
                if (ly >= y && ly < y + h) {
                    graphics.fill(x, ly, x + w, ly + 1, color);
                }
            } else {
                int lx = x + (int) line.pos;
                if (lx >= x && lx < x + w) {
                    graphics.fill(lx, y, lx + 1, y + h, color);
                }
            }
        }
    }

    private void drawStars(GuiGraphics graphics, int x, int y, int w, int h) {
        for (BackgroundStar star : stars) {
            float twinkle = Mth.sin(driftPhase * star.twinkleSpeed * 60f + star.twinkleOffset);
            float brightness = 0.5f + 0.5f * twinkle;
            int alpha = (int) (0xCC * brightness);

            int starX = x + (int) star.x;
            int starY = y + (int) star.y;

            if (starX < x || starX >= x + w || starY < y || starY >= y + h) continue;

            int color = (alpha << 24) | (star.color & 0x00FFFFFF);

            if (star.size > 1.0f) {
                graphics.fill(starX - 1, starY, starX + 2, starY + 1, color);
                graphics.fill(starX, starY - 1, starX + 1, starY + 2, color);
            } else {
                graphics.fill(starX, starY, starX + 1, starY + 1, color);
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
