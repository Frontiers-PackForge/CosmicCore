package com.ghostipedia.cosmiccore.client;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.food.FoodBar;
import com.ghostipedia.cosmiccore.common.item.behavior.WirelessPDABehavior;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.IItemHUDProvider;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@NoArgsConstructor
public class CosmicHudGuiOverlay implements LayeredDraw.Layer {

    // Oxygen bar textures
    private static final ResourceLocation OXY_BG = CosmicCore.id("textures/gui/oxygen_bg.png");
    private static final ResourceLocation OXY_FILL = CosmicCore.id("textures/gui/oxygen_fill.png");
    private static final int TEX_W = 64, TEX_H = 12;

    // Time bar state
    private static long timeTicksLeft = -1;
    private static long timeMaxTicks = 0;

    // Oxygen bar state
    private static long oxygenTicksLeft = -1;
    private static long oxygenMaxTicks = 0;
    private static boolean oxygenShow = true;
    private static double lastRateTicksPerSecond = Double.NaN;

    // Track displayed value to prevent visual jitter (bar only moves in direction of rate)
    private static long displayedOxygen = -1;

    // Colors for oxygen bar text
    private static final int COLOR_DRAIN = 0x000000;  // Black for draining (no shadow)
    private static final int COLOR_REGEN = 0x00ff66;
    private static final int COLOR_IDLE = 0xAAAAAA;

    public static void setTimeBar(ResourceLocation dim, long left, long max) {
        timeTicksLeft = left;
        timeMaxTicks = max;
    }

    public static void setOxygenBar(long left, long max, boolean show, double ratePerSecond) {
        oxygenTicksLeft = left;
        oxygenMaxTicks = max;
        oxygenShow = show;
        lastRateTicksPerSecond = ratePerSecond;

        // Update displayed value with monotonic constraint based on rate direction
        // This prevents visual jitter from server-side fluctuations
        if (displayedOxygen < 0) {
            // First sync - just use the value
            displayedOxygen = left;
        } else if (ratePerSecond < -0.1) {
            // Draining: bar can only decrease or stay same
            displayedOxygen = Math.min(displayedOxygen, left);
        } else if (ratePerSecond > 0.1) {
            // Regenerating: bar can only increase or stay same
            displayedOxygen = Math.max(displayedOxygen, left);
        } else {
            // Idle/neutral: snap to actual value
            displayedOxygen = left;
        }
    }

    private static List<FoodBar> foodBars = List.of();
    private static List<FoodBar> brewBars = List.of();
    private static long barsGameTime = -1;

    public static void setFoodData(List<FoodBar> foods, List<FoodBar> brews) {
        foodBars = foods;
        brewBars = brews;
        Minecraft mc = Minecraft.getInstance();
        barsGameTime = mc.level != null ? mc.level.getGameTime() : -1;
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && !mc.gui.getDebugOverlay().showDebugScreen() && !mc.options.hideGui) {
            int screenWidth = guiGraphics.guiWidth();
            int screenHeight = guiGraphics.guiHeight();
            renderHUDWirelessPDA(WirelessPDABehavior.CosmicCuriosUtils.getPDACurio(mc.player), guiGraphics);
            renderTimeBudgetBar(guiGraphics, screenWidth, screenHeight);
            renderOxygenBar(guiGraphics, screenWidth, screenHeight);
            renderFoodSlots(guiGraphics, screenWidth, screenHeight);
        }
    }

    private void renderFoodSlots(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        if (foodBars.isEmpty() && brewBars.isEmpty()) return;
        long elapsed = 0;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && barsGameTime >= 0) {
            elapsed = Math.max(0, mc.level.getGameTime() - barsGameTime);
        }
        int centerX = screenWidth / 2;
        int hotbarTop = screenHeight - 22;
        renderEndcapColumn(guiGraphics, mc, foodBars, centerX - 107, hotbarTop, elapsed);
        renderEndcapColumn(guiGraphics, mc, brewBars, centerX + 95, hotbarTop, elapsed);
    }

    private static void renderEndcapColumn(GuiGraphics guiGraphics, Minecraft mc, List<FoodBar> bars,
                                           int cellX, int hotbarTop, long elapsed) {
        int stride = 22;
        for (int i = 0; i < bars.size(); i++) {
            FoodBar bar = bars.get(i);
            int cellTop = hotbarTop - 3 - i * stride;

            var pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate(cellX, cellTop, 0);
            pose.scale(0.75f, 0.75f, 1f);
            guiGraphics.renderItem(bar.icon(), 0, 0);
            pose.popPose();

            int remaining = (int) Math.max(0, bar.ticksLeft() - elapsed);
            int color = remaining > bar.base() ? 0xFF55FF55 : remaining <= 200 ? 0xFFFF5555 : 0xFFFFFFFF;
            String label = formatSeconds(remaining / 20);
            pose.pushPose();
            pose.translate(cellX + 6, cellTop + 13, 0);
            pose.scale(0.75f, 0.75f, 1f);
            int w = mc.font.width(label);
            guiGraphics.drawString(mc.font, label, -w / 2, 0, color, true);
            pose.popPose();
        }
    }

    private static void renderHUDWirelessPDA(@NotNull ItemStack stack, GuiGraphics guiGraphics) {
        if (stack.getItem() instanceof ComponentItem valueItem) {
            for (IItemComponent behaviour : valueItem.getComponents()) {
                if (behaviour instanceof IItemHUDProvider provider) {
                    IItemHUDProvider.tryDrawHud(provider, stack, guiGraphics);
                }
            }
        }
    }

    private static void renderTimeBudgetBar(GuiGraphics gg, int sw, int sh) {
        if (timeTicksLeft < 0 || timeMaxTicks <= 0) return;

        int w = Math.min(sw - 20, 200);
        int h = 6;
        int x = (sw - w) / 2;
        int y = sh - 335; // above hotbar; tweak if it overlaps other overlays

        double frac = Math.max(0d, Math.min(1d, (double) timeTicksLeft / (double) timeMaxTicks));
        int filled = (int) (w * frac);

        // background & filled bar
        gg.fill(x, y, x + w, y + h, 0xAA000000);
        gg.fill(x, y, x + filled, y + h, 0xAAFF5555);

        // mm:ss label
        long sec = Math.max(0, timeTicksLeft / 20);
        String txt = (sec / 60) + ":" + String.format("%02d", (sec % 60));
        gg.drawString(Minecraft.getInstance().font, txt,
                x + w / 2 - Minecraft.getInstance().font.width(txt) / 2,
                y - 10, 0xFFFFFF, true);
    }

    // -------------------------------------------------------------------------
    // Oxygen Bar Rendering
    // Uses matrix scaling to render at 81px wide (matching hunger bar) without texture stretching
    // -------------------------------------------------------------------------

    // Scale factor to match vanilla hunger bar width (81px target / 64px texture)
    private static final float BAR_SCALE = 81f / TEX_W;  // ~1.265625

    private static void renderOxygenBar(GuiGraphics gg, int screenWidth, int screenHeight) {
        if (!oxygenShow || oxygenTicksLeft < 0 || oxygenMaxTicks <= 0) return;

        // Final rendered dimensions after scaling
        int renderedWidth = (int) (TEX_W * BAR_SCALE);
        int renderedHeight = (int) (TEX_H * BAR_SCALE);

        // Position to match vanilla hunger bar (right edge at screenWidth/2 + 91)
        int x = screenWidth / 2 + 10;
        int y = screenHeight - 29 - renderedHeight;

        // Use displayedOxygen for visual (has monotonic constraint to prevent jitter)
        long visualOxygen = displayedOxygen >= 0 ? displayedOxygen : oxygenTicksLeft;
        double frac = Math.max(0d, Math.min(1d, (double) visualOxygen / (double) oxygenMaxTicks));

        // Calculate filled width in texture pixels (before scaling)
        int filledTexW = (int) (TEX_W * frac);
        filledTexW = Math.max(0, Math.min(TEX_W, filledTexW));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        // Push matrix and apply scale transform
        var pose = gg.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(BAR_SCALE, BAR_SCALE, 1f);

        // Background - render at native texture size (scaling handled by matrix)
        gg.blit(OXY_BG, 0, 0, 0, 0, TEX_W, TEX_H, TEX_W, TEX_H);

        // Fill bar - clip horizontally based on fill amount
        if (filledTexW > 0) {
            gg.blit(OXY_FILL, 0, 0, 0, 0, filledTexW, TEX_H, TEX_W, TEX_H);
        }

        pose.popPose();

        // ETA text - render outside the scaled context for crisp text
        var font = Minecraft.getInstance().font;
        var comp = computeOxygenETA();
        int tx = x + renderedWidth / 2 - font.width(comp) / 2;
        int ty = y + (renderedHeight - 8) / 2 + 1;  // +1 to nudge down for better centering
        // Disable shadow for draining (black text), enable for others
        boolean useShadow = !isDraining();
        gg.drawString(font, comp, tx, ty, 0xFFFFFF, useShadow);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private static boolean isDraining() {
        if (oxygenTicksLeft <= 0) return true;  // Suffocating
        if (oxygenTicksLeft >= oxygenMaxTicks) return false;  // Full
        double r = lastRateTicksPerSecond;
        return Double.isFinite(r) && r < -0.01;  // Negative rate = draining
    }

    private static Component computeOxygenETA() {
        if (oxygenTicksLeft <= 0) {
            return Component.literal("SUFFOCATING").withStyle(s -> s.withColor(COLOR_DRAIN));
        }
        if (oxygenTicksLeft >= oxygenMaxTicks) {
            return Component.literal("--:--").withStyle(s -> s.withColor(COLOR_IDLE));
        }

        double r = lastRateTicksPerSecond;
        if (!Double.isFinite(r) || Math.abs(r) < 0.01) {
            return Component.literal("--:--").withStyle(s -> s.withColor(COLOR_IDLE));
        }

        if (r < 0) {
            // Draining
            long etaSec = (long) Math.ceil(oxygenTicksLeft / (-r));
            return Component.literal("<- " + formatSeconds(etaSec) + "  >").withStyle(s -> s.withColor(COLOR_DRAIN));
        } else {
            // Regenerating
            long ticksNeeded = oxygenMaxTicks - oxygenTicksLeft;
            long etaSec = (long) Math.ceil(ticksNeeded / r);
            return Component.literal("<  " + formatSeconds(etaSec) + " ->").withStyle(s -> s.withColor(COLOR_REGEN));
        }
    }

    private static String formatSeconds(long sec) {
        if (sec < 0) sec = 0;
        long m = sec / 60;
        long s = sec % 60;
        return m + ":" + String.format("%02d", s);
    }
}
