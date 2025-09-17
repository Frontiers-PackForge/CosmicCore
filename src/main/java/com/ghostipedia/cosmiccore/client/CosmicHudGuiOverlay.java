package com.ghostipedia.cosmiccore.client;

import com.ghostipedia.cosmiccore.common.item.behavior.WirelessPDABehavior;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.IItemHUDProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class CosmicHudGuiOverlay implements IGuiOverlay {

    private static long timeTicksLeft = -1;
    private static long timeMaxTicks = 0;

    private static long oxygenTicksLeft = -1;
    private static long oxygenMaxTicks  = 0;
    private static boolean oxygenShow   = true;

    // --- ETA estimator state ---
    private static long lastSampleGameTime = -1;
    private static long lastSampleOxygenTicks = -1;
    /** Signed ticks/sec: negative = depleting, positive = regenerating. */
    private static double estRateTicksPerSecond = 0.0;
    /** EMA smoothing factor (0..1). Higher = snappier, lower = smoother. */
    private static final double RATE_EMA_ALPHA = 0.4;


    public static void setTimeBar(ResourceLocation dim, long left, long max) {
        timeTicksLeft = left;
        timeMaxTicks = max;
    }

    public static void setOxygenBar(long left, long max, boolean show) {
        var mc = Minecraft.getInstance();
        long nowGameTime = (mc.level != null) ? mc.level.getGameTime() : -1;

        // Update EMA rate if we have a previous sample and game time
        if (nowGameTime >= 0 && lastSampleGameTime >= 0) {
            long dtTicks = Math.max(1, nowGameTime - lastSampleGameTime); // client ticks
            long dOxy = left - lastSampleOxygenTicks;                      // ticks (neg = deplete)
            double seconds = dtTicks / 20.0;
            double sampleRate = dOxy / seconds;                            // ticks per second (signed)

            if (Double.isFinite(sampleRate)) {
                // clamp wild spikes a bit
                double clamped = Math.max(-40.0, Math.min(40.0, sampleRate));
                estRateTicksPerSecond = RATE_EMA_ALPHA * clamped + (1.0 - RATE_EMA_ALPHA) * estRateTicksPerSecond;
            }
        }

        lastSampleGameTime = nowGameTime;
        lastSampleOxygenTicks = left;

        oxygenTicksLeft = left;
        oxygenMaxTicks = max;
        oxygenShow = show;
    }


    @Override
    public void render(ForgeGui forgeGui, GuiGraphics guiGraphics, float partialTick, int screenWidth,
                       int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && !mc.options.renderDebug && !mc.options.hideGui) {
            renderHUDWirelessPDA(WirelessPDABehavior.CosmicCuriosUtils.getPDACurio(mc.player), guiGraphics);
            renderTimeBudgetBar(guiGraphics, screenWidth, screenHeight);
            renderOxygenBar(guiGraphics, screenWidth, screenHeight);
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

    private static void renderOxygenBar(GuiGraphics gg, int screenWidth, int screenHeight) {
        if (!oxygenShow || oxygenTicksLeft < 0 || oxygenMaxTicks <= 0) return;

        int barWidth = Math.max(60, Math.min(78, (int)(screenWidth * 0.35f)));
        int barHeight = 10;
        int x = (screenWidth - barWidth) / 2 + 50;

        int bottomSafePad = 50;  // leave room above the hotbar
        int y = screenHeight - bottomSafePad;

        double frac = Math.max(0d, Math.min(1d, (double) oxygenTicksLeft / (double) oxygenMaxTicks));
        int filled = (int) (barWidth * frac);

        // background + fill
        gg.fill(x, y, x + barWidth, y + barHeight, 0xAA000000);
        gg.fill(x, y, x + filled,   y + barHeight, 0xAA55FFFF);

        // --- ETA text ---
        String txt = computeOxygenEtaLabel();
        int tx = x + barWidth / 2 - Minecraft.getInstance().font.width(txt) / 2;
        int ty = y + 1;
        gg.drawString(Minecraft.getInstance().font, txt, tx, ty, 0xFFFFFF, true);
    }

    private static String computeOxygenEtaLabel() {
        if (oxygenTicksLeft <= 0) return "SUFFOCATING";

        //Negative = depleting, Positive = regenerating.
        double r = estRateTicksPerSecond;

        // ETA to 0 Breath
        if (r < -0.1) {
            double depletePerSec = -r; // make positive
            long etaSec = (long) Math.ceil(oxygenTicksLeft / depletePerSec);
            return "ETA " + formatSeconds(etaSec);
        }

        // If clearly regenerating and not full, show time to full (prefixed with +)
        if (r > 0.1 && oxygenTicksLeft < oxygenMaxTicks) {
            long ticksNeeded = oxygenMaxTicks - oxygenTicksLeft;
            long etaSec = (long) Math.ceil(ticksNeeded / r);
            return formatSeconds(etaSec);
        }

        // Otherwise: steady/unknown -> infinite
        return "∞";
    }

    private static String formatSeconds(long sec) {
        if (sec < 0) sec = 0;
        long m = sec / 60;
        long s = sec % 60;
        return m + ":" + String.format("%02d", s);
    }



    // -------------------------------------------------------------------------
}
