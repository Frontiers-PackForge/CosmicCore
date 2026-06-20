package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine.Stage;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

/**
 * Full-screen background widget that provides unified visual styling
 * for the entire Stellar Iris UI, including the inventory area.
 */
public class StellarBackgroundWidget extends Widget {

    private final Supplier<Stage> stageSupplier;
    private float animPhase = 0f;

    public StellarBackgroundWidget(int x, int y, int width, int height, Supplier<Stage> stageSupplier) {
        super(x, y, width, height);
        this.stageSupplier = stageSupplier;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        animPhase += 0.02f;
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

        DrawerHelper.drawGradientRect(graphics, x, y, w, h, 0xFF0c0c12, 0xFF060608, false);

        drawGridPattern(graphics, x, y, w, h);

        drawSidePanels(graphics, x, y, w, h, stage);

        int accentColor = getStageAccentColor(stage, 0.4f);
        drawCornerAccents(graphics, x, y, w, h, accentColor);

        int borderColor = getStageAccentColor(stage, 0.2f);
        DrawerHelper.drawBorder(graphics, x, y, w, h, borderColor, 1);
    }

    private void drawGridPattern(GuiGraphics graphics, int x, int y, int w, int h) {
        int gridColor = 0x08FFFFFF;
        int spacing = 16;

        // Vertical lines
        for (int gx = x + spacing; gx < x + w; gx += spacing) {
            graphics.fill(gx, y, gx + 1, y + h, gridColor);
        }

        // Horizontal lines
        for (int gy = y + spacing; gy < y + h; gy += spacing) {
            graphics.fill(x, gy, x + w, gy + 1, gridColor);
        }
    }

    private void drawCornerAccents(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        int len = 20;
        int thickness = 2;

        // Top-left
        graphics.fill(x, y, x + len, y + thickness, color);
        graphics.fill(x, y, x + thickness, y + len, color);

        // Top-right
        graphics.fill(x + w - len, y, x + w, y + thickness, color);
        graphics.fill(x + w - thickness, y, x + w, y + len, color);

        // Bottom-left
        graphics.fill(x, y + h - thickness, x + len, y + h, color);
        graphics.fill(x, y + h - len, x + thickness, y + h, color);

        // Bottom-right
        graphics.fill(x + w - len, y + h - thickness, x + w, y + h, color);
        graphics.fill(x + w - thickness, y + h - len, x + w, y + h, color);
    }

    private void drawSidePanels(GuiGraphics graphics, int x, int y, int w, int h, Stage stage) {
        int invWidth = 162;
        int invX = x + (w - invWidth) / 2;

        int leftPanelW = invX - x - 5;
        if (leftPanelW > 10) {
            drawTechPanel(graphics, x + 3, y + h - 85, leftPanelW, 80, stage, true);
        }

        int rightPanelX = invX + invWidth + 5;
        int rightPanelW = (x + w) - rightPanelX - 3;
        if (rightPanelW > 10) {
            drawStatsPanel(graphics, rightPanelX, y + h - 85, rightPanelW, 80, stage);
        }
    }

    private void drawStatsPanel(GuiGraphics graphics, int px, int py, int pw, int ph, Stage stage) {
        DrawerHelper.drawSolidRect(graphics, px, py, pw, ph, 0x40000000);

        int borderColor = getStageAccentColor(stage, 0.2f);
        DrawerHelper.drawBorder(graphics, px, py, pw, ph, borderColor, 1);

        int accentColor = getStageAccentColor(stage, 0.5f);
        graphics.fill(px + 1, py + 1, px + pw - 1, py + 3, accentColor);

        var font = net.minecraft.client.Minecraft.getInstance().font;
        int labelColor = 0xFF606080;
        int valueColor = 0xFFCCCCCC;

        graphics.drawString(font, "STAR STATS", px + 4, py + 6, accentColor, false);

        float temp = getStageTemp(stage);
        float mass = getStageMass(stage);
        float output = getStageOutput(stage);

        int row1 = py + 20;
        int row2 = py + 32;
        int row3 = py + 44;
        int row4 = py + 56;

        graphics.drawString(font, "TEMP:", px + 4, row1, labelColor, false);
        graphics.drawString(font, formatTemp(temp), px + 35, row1, getTemperatureColor(temp), false);

        graphics.drawString(font, "MASS:", px + 4, row2, labelColor, false);
        graphics.drawString(font, String.format("%.1f M\u2609", mass), px + 35, row2, valueColor, false);

        graphics.drawString(font, "OUT:", px + 4, row3, labelColor, false);
        graphics.drawString(font, formatEnergy(output), px + 30, row3, valueColor, false);

        String status = getStatusString(stage);
        graphics.drawString(font, status, px + 4, row4, getStatusColor(stage), false);
    }

    private float getStageTemp(Stage stage) {
        return switch (stage) {
            case EMPTY -> 2.7f;
            case GROWING -> 5_000_000f;
            case STAR -> 15_000_000f;
            case SUPERSTAR -> 100_000_000f;
            case BLACK_HOLE -> Float.POSITIVE_INFINITY;
            case DEATH -> 500_000_000f;
            case DEATH_GRACEFUL -> 1_000_000f;
        };
    }

    private float getStageMass(Stage stage) {
        return switch (stage) {
            case EMPTY -> 0f;
            case GROWING -> 0.3f;
            case STAR -> 1f;
            case SUPERSTAR -> 8f;
            case BLACK_HOLE -> 25f;
            case DEATH -> 12f;
            case DEATH_GRACEFUL -> 0.1f;
        };
    }

    private float getStageOutput(Stage stage) {
        return switch (stage) {
            case EMPTY -> 0f;
            case GROWING -> 1_000f;
            case STAR -> 50_000f;
            case SUPERSTAR -> 500_000f;
            case BLACK_HOLE -> 10_000_000f;
            case DEATH -> 100_000_000f;
            case DEATH_GRACEFUL -> 500f;
        };
    }

    private String formatTemp(float temp) {
        if (Float.isInfinite(temp)) return "\u221E K";
        if (temp >= 1_000_000) return String.format("%.0fM K", temp / 1_000_000);
        if (temp >= 1000) return String.format("%.0fk K", temp / 1000);
        return String.format("%.1f K", temp);
    }

    private String formatEnergy(float energy) {
        if (energy >= 1_000_000) return String.format("%.1f PW", energy / 1_000_000);
        if (energy >= 1000) return String.format("%.0f TW", energy / 1000);
        return String.format("%.0f GW", energy);
    }

    private int getTemperatureColor(float temp) {
        if (temp >= 100_000_000) return 0xFFFF4444;
        if (temp >= 10_000_000) return 0xFFFFAA44;
        if (temp >= 1_000_000) return 0xFFFFFF44;
        return 0xFFCCCCCC;
    }

    private String getStatusString(Stage stage) {
        return switch (stage) {
            case EMPTY -> "DORMANT";
            case GROWING -> "IGNITING";
            case STAR -> "STABLE";
            case SUPERSTAR -> "CRITICAL";
            case BLACK_HOLE -> "CONTAINED";
            case DEATH -> "FAILURE";
            case DEATH_GRACEFUL -> "SHUTDOWN";
        };
    }

    private int getStatusColor(Stage stage) {
        return switch (stage) {
            case EMPTY -> 0xFF606060;
            case GROWING -> 0xFF66AAFF;
            case STAR -> 0xFF66FF66;
            case SUPERSTAR -> 0xFFFFAA44;
            case BLACK_HOLE -> 0xFFAA66FF;
            case DEATH -> 0xFFFF4444;
            case DEATH_GRACEFUL -> 0xFF886666;
        };
    }

    private void drawTechPanel(GuiGraphics graphics, int px, int py, int pw, int ph, Stage stage, boolean isLeft) {
        // Panel background
        DrawerHelper.drawSolidRect(graphics, px, py, pw, ph, 0x40000000);

        // Border
        int borderColor = getStageAccentColor(stage, 0.2f);
        DrawerHelper.drawBorder(graphics, px, py, pw, ph, borderColor, 1);

        // Accent line at top
        int accentColor = getStageAccentColor(stage, 0.5f);
        graphics.fill(px + 1, py + 1, px + pw - 1, py + 3, accentColor);

        // Animated scan line
        float scanPos = (animPhase * 0.5f) % 1f;
        int scanY = py + 5 + (int) ((ph - 10) * scanPos);
        int scanColor = getStageAccentColor(stage, 0.15f);
        graphics.fill(px + 2, scanY, px + pw - 2, scanY + 2, scanColor);

        // Tech decoration lines
        int lineColor = 0x20FFFFFF;
        int lineY = py + 15;
        for (int i = 0; i < 5 && lineY + 10 < py + ph; i++) {
            int lineW = (int) ((pw - 10) * (0.3f + 0.5f * Math.abs(Mth.sin(animPhase + i * 0.5f))));
            if (isLeft) {
                graphics.fill(px + 5, lineY, px + 5 + lineW, lineY + 2, lineColor);
            } else {
                graphics.fill(px + pw - 5 - lineW, lineY, px + pw - 5, lineY + 2, lineColor);
            }
            lineY += 12;
        }

        // Small blinking indicators
        int indicatorY = py + ph - 15;
        for (int i = 0; i < 3; i++) {
            int ix = isLeft ? (px + 8 + i * 8) : (px + pw - 8 - i * 8 - 4);
            boolean blink = ((int) (animPhase * 3 + i) % 3) == 0;
            int indColor = blink ? getStageAccentColor(stage, 0.8f) : 0x30404050;
            graphics.fill(ix, indicatorY, ix + 4, indicatorY + 4, indColor);
        }
    }

    private int getStageAccentColor(Stage stage, float alpha) {
        int a = (int) (alpha * 255) << 24;
        return switch (stage) {
            case EMPTY -> a | 0x404060;
            case GROWING -> a | 0x6080FF;
            case STAR -> a | 0xFFCC44;
            case SUPERSTAR -> a | 0xFF8844;
            case BLACK_HOLE -> a | 0x8040FF;
            case DEATH -> a | 0xFF2020;
            case DEATH_GRACEFUL -> a | 0x804040;
        };
    }
}
