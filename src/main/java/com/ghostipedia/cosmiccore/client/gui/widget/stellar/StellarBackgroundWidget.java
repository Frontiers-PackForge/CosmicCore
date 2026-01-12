package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine.Stage;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

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

        // Main dark background covering full area
        DrawerHelper.drawGradientRect(graphics, x, y, w, h, 0xFF12121a, 0xFF08080c, false);

        // Subtle grid pattern for tech feel
        drawGridPattern(graphics, x, y, w, h);

        // Corner accents with stage color
        int accentColor = getStageAccentColor(stage, 0.4f);
        drawCornerAccents(graphics, x, y, w, h, accentColor);

        // Side panel decorations (where the white space was)
        drawSidePanels(graphics, x, y, w, h, stage);

        // Outer border
        int borderColor = getStageAccentColor(stage, 0.25f);
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
        // Calculate where the inventory is (roughly centered, 9 slots wide = 162px)
        int invWidth = 162;
        int invX = x + (w - invWidth) / 2;

        // Left panel area
        int leftPanelW = invX - x - 5;
        if (leftPanelW > 10) {
            drawTechPanel(graphics, x + 3, y + h - 85, leftPanelW, 80, stage, true);
        }

        // Right panel area
        int rightPanelX = invX + invWidth + 5;
        int rightPanelW = (x + w) - rightPanelX - 3;
        if (rightPanelW > 10) {
            drawTechPanel(graphics, rightPanelX, y + h - 85, rightPanelW, 80, stage, false);
        }
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
