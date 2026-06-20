package com.ghostipedia.cosmiccore.client.gui.widget.starladder;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Random;
import java.util.function.IntSupplier;

import javax.annotation.Nonnull;

/**
 * Fake telemetry panel that displays random sci-fi research data.
 * Purely aesthetic - vomits technobabble for immersion.
 */
public class TelemetryPanelWidget extends Widget {

    private static final int[] TIER_COLORS = {
            0xFF4080C0, // T0 - Blue
            0xFF40C080, // T1 - Green
            0xFFC0A040, // T2 - Gold
            0xFFC040C0  // T3 - Purple
    };

    // Each metric paired with an appropriate unit
    private static final String[][] METRICS = {
            { "SOUL OUTPUT", "kSU" },      // kilo-Soul Units
            { "SOLAR RADS", "Sv/h" },      // Sieverts per hour (radiation)
            { "CABLE STR", "%" },          // percentage strength
            { "DATA RATE", "Gb/s" },       // Gigabits per second
            { "PACKET LOSS", "%" },        // percentage
            { "ERROR MARGIN", "ppm" },     // parts per million
            { "WEAR", "%" }                // percentage wear
    };

    private static final String[] STATUS_LABELS = {
            "NOMINAL", "STABLE", "ACTIVE", "SYNCED", "OPTIMAL"
    };

    private final IntSupplier tierSupplier;
    private final Random random = new Random();

    private float animPhase = 0f;
    private float scanLineY = 0f;

    // Cached "readings" that update periodically
    private int[] currentMetricIndices = new int[4];
    private float[] metricValues = new float[4];
    private float[] targetValues = new float[4];
    private int tickCounter = 0;

    public TelemetryPanelWidget(int x, int y, int width, int height, IntSupplier tierSupplier) {
        super(x, y, width, height);
        this.tierSupplier = tierSupplier;
        initializeMetrics();
    }

    private void initializeMetrics() {
        for (int i = 0; i < 4; i++) {
            currentMetricIndices[i] = random.nextInt(METRICS.length);
            metricValues[i] = random.nextFloat() * 100f;
            targetValues[i] = random.nextFloat() * 100f;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        animPhase += 0.08f;
        scanLineY += 0.5f;
        if (scanLineY > getSize().height) {
            scanLineY = 0f;
        }

        tickCounter++;

        // Smoothly interpolate values
        for (int i = 0; i < 4; i++) {
            metricValues[i] = Mth.lerp(0.05f, metricValues[i], targetValues[i]);
        }

        // Periodically update targets and occasionally swap metrics
        if (tickCounter % 20 == 0) {
            for (int i = 0; i < 4; i++) {
                targetValues[i] = targetValues[i] + (random.nextFloat() - 0.5f) * 20f;
                targetValues[i] = Mth.clamp(targetValues[i], 0f, 100f);
            }
        }

        if (tickCounter % 100 == 0) {
            int idx = random.nextInt(4);
            currentMetricIndices[idx] = random.nextInt(METRICS.length);
            targetValues[idx] = random.nextFloat() * 100f;
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

        int tier = tierSupplier.getAsInt();
        int tierColor = getTierColor(tier);

        // Background
        DrawerHelper.drawSolidRect(graphics, x, y, w, h, 0xC0081018);
        DrawerHelper.drawBorder(graphics, x, y, w, h, adjustAlpha(tierColor, 0.4f), 1);

        // Scan line effect
        int scanAlpha = (int) (0x20 * (1f - (scanLineY / h)));
        int scanColor = (scanAlpha << 24) | (tierColor & 0x00FFFFFF);
        graphics.fill(x + 1, y + (int) scanLineY, x + w - 1, y + (int) scanLineY + 2, scanColor);

        // Draw telemetry data
        drawTelemetryLines(graphics, x, y, w, h, tier, tierColor);
    }

    private void drawTelemetryLines(GuiGraphics graphics, int x, int y, int w, int h, int tier, int tierColor) {
        var font = Minecraft.getInstance().font;
        int lineHeight = 22;
        int startY = y + 4;

        // Show up to 4 metrics
        int maxMetrics = Math.min(4, currentMetricIndices.length);

        for (int i = 0; i < maxMetrics; i++) {
            int lineY = startY + i * lineHeight;
            if (lineY + lineHeight > y + h - 16) break;

            // Get metric name and unit from the paired array
            int metricIdx = currentMetricIndices[i];
            String metricName = METRICS[metricIdx][0];
            String unit = METRICS[metricIdx][1];

            // Metric name on the left (dim color)
            graphics.drawString(font, metricName, x + 4, lineY, 0xFF606080, false);

            // Value with fluctuation
            float value = metricValues[i];
            float displayValue = value + Mth.sin(animPhase + i * 1.3f) * (tier + 1) * 0.5f;
            String valueStr = String.format("%.1f", displayValue);

            // Color based on tier and value
            int valueColor = getValueColor(displayValue, tierColor);
            float pulse = Mth.sin(animPhase * 2f + i) * 0.1f + 0.9f;
            valueColor = adjustAlpha(valueColor, pulse);

            // Draw value and unit on right side
            String valueWithUnit = valueStr + unit;
            int valueX = x + w - font.width(valueWithUnit) - 4;
            graphics.drawString(font, valueStr, valueX, lineY, valueColor, false);
            graphics.drawString(font, unit, valueX + font.width(valueStr), lineY, 0xFF505060, false);

            // Mini bar indicator below
            int barX = x + 4;
            int barY = lineY + font.lineHeight + 1;
            int barW = w - 8;
            int barH = 2;

            // Background bar
            graphics.fill(barX, barY, barX + barW, barY + barH, 0xFF202030);

            // Fill bar
            int fillW = (int) (barW * (value / 100f));
            int barColor = adjustAlpha(tierColor, 0.6f);
            graphics.fill(barX, barY, barX + fillW, barY + barH, barColor);

            // Pulse dot at end
            if (fillW > 2) {
                float dotPulse = Mth.sin(animPhase * 3f + i * 0.7f) * 0.3f + 0.7f;
                int dotAlpha = (int) (0xFF * dotPulse);
                int dotColor = (dotAlpha << 24) | (tierColor & 0x00FFFFFF);
                graphics.fill(barX + fillW - 2, barY - 1, barX + fillW, barY + barH + 1, dotColor);
            }
        }

        // Status line at bottom
        String status = STATUS_LABELS[tier % STATUS_LABELS.length];
        float statusPulse = Mth.sin(animPhase) * 0.15f + 0.85f;
        int statusAlpha = (int) (0xFF * statusPulse);
        int statusColor = (statusAlpha << 24) | (tierColor & 0x00FFFFFF);

        int statusY = y + h - font.lineHeight - 3;
        graphics.drawString(font, "[" + status + "]", x + 4, statusY, statusColor, false);
    }

    private int getValueColor(float value, int tierColor) {
        if (value > 80f) {
            return 0xFF40FF80; // Green - high
        } else if (value > 50f) {
            return tierColor; // Tier color - normal
        } else if (value > 20f) {
            return 0xFFC0A040; // Yellow - low
        } else {
            return 0xFFFF6040; // Red - critical
        }
    }

    private int getTierColor(int tier) {
        if (tier < 0) return TIER_COLORS[0];
        if (tier >= TIER_COLORS.length) return TIER_COLORS[TIER_COLORS.length - 1];
        return TIER_COLORS[tier];
    }

    private int adjustAlpha(int color, float factor) {
        int a = (int) (((color >> 24) & 0xFF) * factor);
        return (a << 24) | (color & 0x00FFFFFF);
    }
}
