package com.ghostipedia.cosmiccore.client.gui.widget.starladder;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.IntSupplier;

import javax.annotation.Nonnull;

public class TierDisplayWidget extends Widget {

    private static final int MAX_TIER = 3;
    private static final int[] TIER_COLORS = {
            0xFF4080C0, // T0 - Blue
            0xFF40C080, // T1 - Green
            0xFFC0A040, // T2 - Gold
            0xFFC040C0  // T3 - Purple
    };

    private final IntSupplier tierSupplier;

    private float displayedTier = 0f;
    private float pulsePhase = 0f;
    private float ringRotation = 0f;

    public TierDisplayWidget(int x, int y, int width, int height, IntSupplier tierSupplier) {
        super(x, y, width, height);
        this.tierSupplier = tierSupplier;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        pulsePhase += 0.06f;
        ringRotation += 0.02f;

        float targetTier = tierSupplier.getAsInt();
        displayedTier = Mth.lerp(0.08f, displayedTier, targetTier);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);

        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        int currentTier = tierSupplier.getAsInt();
        int tierColor = getTierColor(currentTier);

        DrawerHelper.drawSolidRect(graphics, x, y, w, h, 0xA0101828);

        int borderColor = adjustAlpha(tierColor, 0.5f);
        DrawerHelper.drawBorder(graphics, x, y, w, h, borderColor, 1);

        drawTierRings(graphics, x, y, w, h, currentTier, tierColor);
        drawTierLabel(graphics, x, y, w, h, currentTier, tierColor);
        drawTierIndicators(graphics, x, y, w, h, currentTier);
    }

    private void drawTierRings(GuiGraphics graphics, int x, int y, int w, int h, int tier, int tierColor) {
        int centerX = x + w / 2;
        int centerY = y + h / 2 - 8;
        int baseRadius = Math.min(w, h) / 3;

        for (int ring = 0; ring <= tier && ring <= MAX_TIER; ring++) {
            float ringProgress = ring <= displayedTier ? 1f : Math.max(0, displayedTier - ring + 1);
            if (ringProgress <= 0) continue;

            int ringRadius = baseRadius + ring * 6;
            int ringColor = getTierColor(ring);

            float pulse = Mth.sin(pulsePhase + ring * 0.5f) * 0.15f + 0.85f;
            int alpha = (int) (0x60 * pulse * ringProgress);
            int color = (alpha << 24) | (ringColor & 0x00FFFFFF);

            drawRing(graphics, centerX, centerY, ringRadius, color);
        }

        float corePulse = Mth.sin(pulsePhase * 1.5f) * 0.2f + 0.8f;
        int coreAlpha = (int) (0xA0 * corePulse);
        int coreColor = (coreAlpha << 24) | (tierColor & 0x00FFFFFF);

        int coreSize = 8;
        graphics.fill(centerX - coreSize / 2, centerY - coreSize / 2,
                centerX + coreSize / 2, centerY + coreSize / 2, coreColor);

        int glowAlpha = (int) (0x30 * corePulse);
        int glowColor = (glowAlpha << 24) | (tierColor & 0x00FFFFFF);
        graphics.fill(centerX - coreSize, centerY - coreSize,
                centerX + coreSize, centerY + coreSize, glowColor);
    }

    private void drawRing(GuiGraphics graphics, int cx, int cy, int radius, int color) {
        int segments = 32;
        for (int i = 0; i < segments; i++) {
            float angle1 = (i * Mth.TWO_PI / segments) + ringRotation;
            float angle2 = ((i + 1) * Mth.TWO_PI / segments) + ringRotation;

            int x1 = cx + (int) (Mth.cos(angle1) * radius);
            int y1 = cy + (int) (Mth.sin(angle1) * radius * 0.4f);
            int x2 = cx + (int) (Mth.cos(angle2) * radius);
            int y2 = cy + (int) (Mth.sin(angle2) * radius * 0.4f);

            if (i % 2 == 0) {
                graphics.fill(x1, y1, x1 + 1, y1 + 1, color);
            }
        }
    }

    private void drawTierLabel(GuiGraphics graphics, int x, int y, int w, int h, int tier, int tierColor) {
        var font = Minecraft.getInstance().font;

        String tierText = "TIER " + tier;
        int textX = x + (w - font.width(tierText)) / 2;
        int textY = y + h - font.lineHeight - 16;

        graphics.drawString(font, tierText, textX, textY, tierColor, false);

        String subText = getTierName(tier);
        int subX = x + (w - font.width(subText)) / 2;
        int subY = textY + font.lineHeight + 2;
        graphics.drawString(font, subText, subX, subY, 0xFF808090, false);
    }

    private void drawTierIndicators(GuiGraphics graphics, int x, int y, int w, int h, int currentTier) {
        int indicatorY = y + h - 8;
        int totalWidth = (MAX_TIER + 1) * 10 - 4;
        int startX = x + (w - totalWidth) / 2;

        for (int i = 0; i <= MAX_TIER; i++) {
            int dotX = startX + i * 10;
            int dotColor;

            if (i <= currentTier) {
                float pulse = Mth.sin(pulsePhase + i * 0.3f) * 0.2f + 0.8f;
                int alpha = (int) (0xFF * pulse);
                dotColor = (alpha << 24) | (getTierColor(i) & 0x00FFFFFF);
            } else {
                dotColor = 0xFF303040;
            }

            graphics.fill(dotX, indicatorY, dotX + 6, indicatorY + 4, dotColor);
        }
    }

    private int getTierColor(int tier) {
        if (tier < 0) return TIER_COLORS[0];
        if (tier >= TIER_COLORS.length) return TIER_COLORS[TIER_COLORS.length - 1];
        return TIER_COLORS[tier];
    }

    private String getTierName(int tier) {
        return switch (tier) {
            case 0 -> "FOUNDATION";
            case 1 -> "EXPANSION";
            case 2 -> "ADVANCED";
            case 3 -> "MAXIMUM";
            default -> "UNKNOWN";
        };
    }

    private int adjustAlpha(int color, float factor) {
        int a = (int) (((color >> 24) & 0xFF) * factor);
        return (a << 24) | (color & 0x00FFFFFF);
    }
}
