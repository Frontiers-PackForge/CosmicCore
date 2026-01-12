package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class FuelGaugeWidget extends Widget {

    private final Supplier<Float> fuelLevelSupplier;

    private float displayedLevel = 0f;
    private float animPhase = 0f;

    public FuelGaugeWidget(int x, int y, int width, int height, Supplier<Float> fuelLevelSupplier) {
        super(x, y, width, height);
        this.fuelLevelSupplier = fuelLevelSupplier;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        animPhase += 0.1f;

        float target = fuelLevelSupplier.get();
        displayedLevel = Mth.lerp(0.1f, displayedLevel, target);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);

        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        // Label
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, "STELLAR FUEL", x, y, 0xFF909090, false);

        // Gauge background
        int gaugeY = y + 12;
        int gaugeH = h - 12;
        DrawerHelper.drawSolidRect(graphics, x, gaugeY, w, gaugeH, 0xFF101020);

        // Border
        DrawerHelper.drawBorder(graphics, x, gaugeY, w, gaugeH, 0xFF303050, 1);

        // Filled portion with gradient based on level
        int fillW = (int) (w * displayedLevel);
        if (fillW > 0) {
            int fillColor = getFillColor(displayedLevel);
            int fillColorDark = darkenColor(fillColor, 0.6f);

            DrawerHelper.drawGradientRect(graphics, x + 1, gaugeY + 1, fillW - 2, gaugeH - 2,
                fillColorDark, fillColor, true);

            // Animated shimmer
            float shimmerPos = (animPhase % (w * 2)) - w;
            if (shimmerPos > 0 && shimmerPos < fillW) {
                int shimmerX = x + (int) shimmerPos;
                int shimmerW = Math.min(10, fillW - (int) shimmerPos);
                graphics.fill(shimmerX, gaugeY + 1, shimmerX + shimmerW, gaugeY + gaugeH - 1,
                    0x20FFFFFF);
            }
        }

        // Threshold markers
        drawThresholdMarker(graphics, x, gaugeY, w, gaugeH, 0.8f, "IGNITE");

        // Percentage text
        int percent = (int) (displayedLevel * 100);
        String percentStr = percent + "%";
        int textX = x + w - font.width(percentStr) - 2;
        int textColor = displayedLevel >= 0.8f ? 0xFF80FF80 : 0xFFFFFFFF;
        graphics.drawString(font, percentStr, textX, gaugeY + (gaugeH - font.lineHeight) / 2 + 1,
            textColor, false);
    }

    private void drawThresholdMarker(GuiGraphics graphics, int x, int y, int w, int h,
                                     float threshold, String label) {
        int markerX = x + (int) (w * threshold);

        // Vertical line
        graphics.fill(markerX, y, markerX + 1, y + h, 0xFF80FF80);

        // Small triangle indicator
        graphics.fill(markerX - 2, y - 3, markerX + 3, y, 0xFF80FF80);
    }

    private int getFillColor(float level) {
        if (level >= 0.8f) {
            return 0xFF40FF60; // Green - ready
        } else if (level >= 0.5f) {
            return 0xFFFFCC40; // Yellow - charging
        } else if (level >= 0.2f) {
            return 0xFFFF8040; // Orange - low
        } else {
            return 0xFFFF4040; // Red - critical
        }
    }

    private int darkenColor(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int) (((color >> 16) & 0xFF) * factor);
        int g = (int) (((color >> 8) & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
