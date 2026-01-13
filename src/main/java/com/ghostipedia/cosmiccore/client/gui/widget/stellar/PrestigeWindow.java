package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

public class PrestigeWindow extends Widget {

    private static final int UPDATE_ID_PRESTIGE_DATA = 410;

    private final Supplier<IrisMultiblockMachine> machineSupplier;
    private final Runnable onClose;

    private boolean visible = false;
    private float fadeAlpha = 0f;
    private float targetAlpha = 0f;

    private int totalPoints = 0;
    private int earnedPoints = 0;
    private int currentTier = 0;
    private int previousTier = 0;

    private int animationTick = 0;
    private float pointCounterDisplay = 0f;
    private boolean showTierUp = false;
    private int tierUpAnimTick = 0;

    private static final int BG_COLOR = 0xFF101020;
    private static final int BORDER_COLOR = 0xFF4060A0;
    private static final int ACCENT_COLOR = 0xFF80A0FF;
    private static final int GOLD_COLOR = 0xFFFFCC44;
    private static final int TEXT_COLOR = 0xFFCCCCDD;
    private static final int TIER_COLORS[] = {
            0xFF808080,
            0xFF60A060,
            0xFF4080C0,
            0xFFA060C0,
            0xFFD08040,
            0xFFFFCC44,
    };

    public PrestigeWindow(int x, int y, int width, int height,
                          Supplier<IrisMultiblockMachine> machineSupplier,
                          Runnable onClose) {
        super(x, y, width, height);
        this.machineSupplier = machineSupplier;
        this.onClose = onClose;
    }

    public void show(int earned, int total, int tier, int prevTier) {
        this.earnedPoints = earned;
        this.totalPoints = total;
        this.currentTier = tier;
        this.previousTier = prevTier;
        this.visible = true;
        this.targetAlpha = 1f;
        this.animationTick = 0;
        this.pointCounterDisplay = 0f;
        this.showTierUp = tier > prevTier;
        this.tierUpAnimTick = 0;
    }

    public void hide() {
        this.targetAlpha = 0f;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();

        if (fadeAlpha < targetAlpha) {
            fadeAlpha = Math.min(fadeAlpha + 0.05f, targetAlpha);
        } else if (fadeAlpha > targetAlpha) {
            fadeAlpha = Math.max(fadeAlpha - 0.05f, targetAlpha);
            if (fadeAlpha <= 0f) {
                visible = false;
            }
        }

        if (!visible) return;

        animationTick++;

        if (animationTick > 20 && pointCounterDisplay < earnedPoints) {
            float remaining = earnedPoints - pointCounterDisplay;
            float speed = Math.max(1f, remaining * 0.15f);
            pointCounterDisplay = Math.min(pointCounterDisplay + speed, earnedPoints);
        }

        if (showTierUp && animationTick > 60) {
            tierUpAnimTick++;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);

        if (!visible && fadeAlpha <= 0f) return;

        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        drawWindowBackground(graphics, x, y, w, h);
        drawHeader(graphics, x, y, w);
        drawPointsSection(graphics, x, y + 30, w);
        drawTierSection(graphics, x, y + 80, w);

        if (showTierUp && tierUpAnimTick > 0) {
            drawTierUpCelebration(graphics, x, y, w, h);
        }

        drawCloseHint(graphics, x, y + h - 16, w);
    }

    private void drawWindowBackground(GuiGraphics graphics, int x, int y, int w, int h) {
        int bgAlpha = (int) (0xFF * fadeAlpha);
        int bgColor = (bgAlpha << 24) | (BG_COLOR & 0x00FFFFFF);
        DrawerHelper.drawSolidRect(graphics, x, y, w, h, bgColor);

        int borderAlpha = (int) (0xFF * fadeAlpha);
        int borderColor = (borderAlpha << 24) | (BORDER_COLOR & 0x00FFFFFF);
        DrawerHelper.drawBorder(graphics, x, y, w, h, borderColor, 2);

        int glowAlpha = (int) (0x40 * fadeAlpha);
        int glowColor = (glowAlpha << 24) | (ACCENT_COLOR & 0x00FFFFFF);
        DrawerHelper.drawGradientRect(graphics, x + 2, y + 2, w - 4, 30, glowColor, 0x00000000, false);
    }

    private void drawHeader(GuiGraphics graphics, int x, int y, int w) {
        var font = Minecraft.getInstance().font;

        String title = Component.translatable("cosmiccore.stellar.prestige.title").getString();
        int titleColor = applyFade(GOLD_COLOR);

        int titleX = x + (w - font.width(title)) / 2;
        int titleY = y + 8;

        graphics.drawString(font, title, titleX + 1, titleY + 1, applyFade(0xFF000000), false);
        graphics.drawString(font, title, titleX, titleY, titleColor, false);

        int lineY = y + 24;
        int lineAlpha = (int) (0x80 * fadeAlpha);
        int lineColor = (lineAlpha << 24) | (ACCENT_COLOR & 0x00FFFFFF);
        graphics.fill(x + 20, lineY, x + w - 20, lineY + 1, lineColor);
    }

    private void drawPointsSection(GuiGraphics graphics, int x, int y, int w) {
        var font = Minecraft.getInstance().font;

        String earnedLabel = Component.translatable("cosmiccore.stellar.prestige.points_earned").getString();
        int labelColor = applyFade(TEXT_COLOR);
        int labelX = x + (w - font.width(earnedLabel)) / 2;
        graphics.drawString(font, earnedLabel, labelX, y, labelColor, false);

        String pointsStr = "+" + (int) pointCounterDisplay;
        int pointsColor = applyFade(GOLD_COLOR);

        float scale = 2.0f;
        int pointsWidth = (int) (font.width(pointsStr) * scale);
        int pointsX = x + (w - pointsWidth) / 2;
        int pointsY = y + 12;

        graphics.pose().pushPose();
        graphics.pose().translate(pointsX, pointsY, 0);
        graphics.pose().scale(scale, scale, 1f);
        graphics.drawString(font, pointsStr, 0, 0, pointsColor, false);
        graphics.pose().popPose();

        String totalLabel = Component.translatable("cosmiccore.stellar.prestige.total_points", totalPoints).getString();
        int totalLabelX = x + (w - font.width(totalLabel)) / 2;
        int totalY = y + 35;
        graphics.drawString(font, totalLabel, totalLabelX, totalY, applyFade(TEXT_COLOR), false);
    }

    private void drawTierSection(GuiGraphics graphics, int x, int y, int w) {
        var font = Minecraft.getInstance().font;

        String tierLabel = Component.translatable("cosmiccore.stellar.prestige.current_tier").getString();
        int labelX = x + (w - font.width(tierLabel)) / 2;
        graphics.drawString(font, tierLabel, labelX, y, applyFade(TEXT_COLOR), false);

        int tierColor = applyFade(getTierColor(currentTier));
        String tierStr = getTierName(currentTier);

        float scale = 1.5f;
        int tierWidth = (int) (font.width(tierStr) * scale);
        int tierX = x + (w - tierWidth) / 2;
        int tierY = y + 12;

        graphics.pose().pushPose();
        graphics.pose().translate(tierX, tierY, 0);
        graphics.pose().scale(scale, scale, 1f);
        graphics.drawString(font, tierStr, 0, 0, tierColor, false);
        graphics.pose().popPose();

        if (currentTier < 5) {
            int nextTierPoints = getPointsForTier(currentTier + 1);
            int currentTierPoints = getPointsForTier(currentTier);
            float progress = (float) (totalPoints - currentTierPoints) / (nextTierPoints - currentTierPoints);
            progress = Mth.clamp(progress, 0f, 1f);

            int barY = y + 35;
            int barW = w - 40;
            int barH = 8;
            int barX = x + 20;

            int barBgColor = applyFade(0xFF202030);
            DrawerHelper.drawSolidRect(graphics, barX, barY, barW, barH, barBgColor);

            int fillW = (int) (barW * progress);
            int fillColor = applyFade(getTierColor(currentTier + 1));
            if (fillW > 0) {
                DrawerHelper.drawSolidRect(graphics, barX, barY, fillW, barH, fillColor);
            }

            DrawerHelper.drawBorder(graphics, barX, barY, barW, barH, applyFade(0xFF404060), 1);

            String nextLabel = Component
                    .translatable("cosmiccore.stellar.prestige.next_tier", nextTierPoints, getTierName(currentTier + 1))
                    .getString();
            int nextLabelX = x + (w - font.width(nextLabel)) / 2;
            graphics.drawString(font, nextLabel, nextLabelX, barY + 10, applyFade(TEXT_COLOR), false);
        } else {
            String maxLabel = Component.translatable("cosmiccore.stellar.prestige.max_tier").getString();
            int maxLabelX = x + (w - font.width(maxLabel)) / 2;
            graphics.drawString(font, maxLabel, maxLabelX, y + 35, applyFade(GOLD_COLOR), false);
        }
    }

    private void drawTierUpCelebration(GuiGraphics graphics, int x, int y, int w, int h) {
        var font = Minecraft.getInstance().font;

        float animProgress = Math.min(1f, tierUpAnimTick / 30f);
        float easeOut = 1f - (1f - animProgress) * (1f - animProgress);

        int bannerH = 40;
        int bannerY = (int) (y - bannerH + easeOut * (h / 2 + bannerH / 2));

        int bannerAlpha = (int) (0xF0 * easeOut * fadeAlpha);
        int bannerColor = (bannerAlpha << 24) | (getTierColor(currentTier) & 0x00FFFFFF);
        DrawerHelper.drawSolidRect(graphics, x + 10, bannerY, w - 20, bannerH, bannerColor);

        String tierUpText = Component.translatable("cosmiccore.stellar.prestige.tier_up").getString();
        String newTierText = getTierName(currentTier);

        int textAlpha = (int) (255 * easeOut * fadeAlpha);
        int textColor = (textAlpha << 24) | 0xFFFFFF;

        float scale = 1.8f;
        int tierUpWidth = (int) (font.width(tierUpText) * scale);
        int tierUpX = x + (w - tierUpWidth) / 2;

        graphics.pose().pushPose();
        graphics.pose().translate(tierUpX, bannerY + 4, 0);
        graphics.pose().scale(scale, scale, 1f);
        graphics.drawString(font, tierUpText, 0, 0, textColor, false);
        graphics.pose().popPose();

        int newTierWidth = font.width(newTierText);
        int newTierX = x + (w - newTierWidth) / 2;
        graphics.drawString(font, newTierText, newTierX, bannerY + 26, textColor, false);

        if (tierUpAnimTick < 40) {
            drawParticleBurst(graphics, x + w / 2, bannerY + bannerH / 2, tierUpAnimTick);
        }
    }

    private void drawParticleBurst(GuiGraphics graphics, int cx, int cy, int tick) {
        int particleCount = 16;
        float progress = tick / 40f;

        for (int i = 0; i < particleCount; i++) {
            float angle = i * Mth.TWO_PI / particleCount;
            float distance = progress * 80;

            int px = cx + (int) (Mth.cos(angle) * distance);
            int py = cy + (int) (Mth.sin(angle) * distance * 0.5f);

            int alpha = (int) ((1f - progress) * 200 * fadeAlpha);
            int color = (alpha << 24) | (GOLD_COLOR & 0x00FFFFFF);

            int size = (int) (3 * (1f - progress));
            if (size > 0) {
                graphics.fill(px - size, py - size, px + size + 1, py + size + 1, color);
            }
        }
    }

    private void drawCloseHint(GuiGraphics graphics, int x, int y, int w) {
        var font = Minecraft.getInstance().font;
        String hint = Component.translatable("cosmiccore.stellar.prestige.continue").getString();
        int hintX = x + (w - font.width(hint)) / 2;
        graphics.drawString(font, hint, hintX, y, applyFade(0xFF808090), false);
    }

    private int applyFade(int color) {
        int a = (color >> 24) & 0xFF;
        a = (int) (a * fadeAlpha);
        return (a << 24) | (color & 0x00FFFFFF);
    }

    private int getTierColor(int tier) {
        if (tier < 0) tier = 0;
        if (tier >= TIER_COLORS.length) tier = TIER_COLORS.length - 1;
        return TIER_COLORS[tier];
    }

    private String getTierName(int tier) {
        return switch (tier) {
            case 0 -> Component.translatable("cosmiccore.stellar.prestige.tier.novice").getString();
            case 1 -> Component.translatable("cosmiccore.stellar.prestige.tier.apprentice").getString();
            case 2 -> Component.translatable("cosmiccore.stellar.prestige.tier.journeyman").getString();
            case 3 -> Component.translatable("cosmiccore.stellar.prestige.tier.expert").getString();
            case 4 -> Component.translatable("cosmiccore.stellar.prestige.tier.master").getString();
            case 5 -> Component.translatable("cosmiccore.stellar.prestige.tier.grandmaster").getString();
            default -> Component.translatable("cosmiccore.stellar.prestige.tier.unknown").getString();
        };
    }

    private int getPointsForTier(int tier) {
        return switch (tier) {
            case 1 -> 50;
            case 2 -> 100;
            case 3 -> 250;
            case 4 -> 500;
            case 5 -> 1000;
            default -> 0;
        };
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (visible && fadeAlpha > 0.5f && animationTick > 40) {
            hide();
            onClose.run();
            return true;
        }
        return false;
    }

    public boolean isVisible() {
        return visible || fadeAlpha > 0f;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        IrisMultiblockMachine machine = machineSupplier.get();
        if (machine == null) return;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == UPDATE_ID_PRESTIGE_DATA) {
            int earned = buffer.readInt();
            int total = buffer.readInt();
            int tier = buffer.readInt();
            int prevTier = buffer.readInt();
            show(earned, total, tier, prevTier);
        } else {
            super.readUpdateInfo(id, buffer);
        }
    }
}
