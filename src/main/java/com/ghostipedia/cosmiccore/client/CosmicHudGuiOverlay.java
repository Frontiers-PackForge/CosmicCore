package com.ghostipedia.cosmiccore.client;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.item.behavior.WirelessPDABehavior;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.IItemHUDProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class CosmicHudGuiOverlay implements IGuiOverlay {

    private static final ResourceLocation OXY_BG   = CosmicCore.id("textures/gui/oxygen_bg.png");
    private static final ResourceLocation OXY_FILL = CosmicCore.id("textures/gui/oxygen_fill.png");
    private static final int TEX_W = 64, TEX_H = 12;
    private static final boolean USE_NINE_SLICE = true;

    private static long timeTicksLeft = -1;
    private static long timeMaxTicks = 0;

    private static long oxygenTicksLeft = -1;
    private static long oxygenMaxTicks  = 0;
    private static boolean oxygenShow   = true;

    private static long lastSampleGameTime = -1;
    private static long lastSampleOxygenTicks = -1;
    private static double lastRateTicksPerSecond = Double.NaN;

    //Designed it to support colors
    //Then decided the colors were hard to read
    //Keeping the functionality in because uhhhhh, maybe i'll use it?????? fuck if i know.
    private static final int COLOR_DRAIN = 0xff6f00;
    private static final int COLOR_REGEN = 0x00ff66;
    private static final int COLOR_IDLE  = 0xAAAAAA;

    public static void setTimeBar(ResourceLocation dim, long left, long max) {
        timeTicksLeft = left;
        timeMaxTicks = max;
    }

    public static void setOxygenBar(long left, long max, boolean show, double ratePerSecond) {
        oxygenTicksLeft = left;
        oxygenMaxTicks  = max;
        oxygenShow      = show;
        lastRateTicksPerSecond = ratePerSecond; // authoritative; no client-side slope needed
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
        int y = sh - 335;

        double frac = Math.max(0d, Math.min(1d, (double) timeTicksLeft / (double) timeMaxTicks));
        int filled = (int) (w * frac);

        gg.fill(x, y, x + w, y + h, 0xAA000000);
        gg.fill(x, y, x + filled, y + h, 0xAAFF5555);

        long sec = Math.max(0, timeTicksLeft / 20);
        String txt = (sec / 60) + ":" + String.format("%02d", (sec % 60));
        gg.drawString(Minecraft.getInstance().font, txt,
                x + w / 2 - Minecraft.getInstance().font.width(txt) / 2,
                y - 10, 0xFFFFFF, true);
    }

    private static final float OXY_WIDTH_FRACTION = 0.13f;
    private static final int   OXY_MIN_W          = 72;
    private static final int   OXY_MAX_W          = 128;
    private static final int   OXY_HEIGHT         = 11;
    private static final int CAP_SRC_PX = 4;


    private static void renderOxygenBar(GuiGraphics gg, int screenWidth, int screenHeight) {
        if (!oxygenShow || oxygenTicksLeft < 0 || oxygenMaxTicks <= 0) return;

        // size & position
        int barWidth  = Math.max(OXY_MIN_W, Math.min(OXY_MAX_W, (int)(screenWidth * OXY_WIDTH_FRACTION)));
        int barHeight = OXY_HEIGHT;
        int x = (screenWidth - barWidth) / 2 + 50;
        int y = screenHeight - 50;

        // progress
        double frac = Math.max(0d, Math.min(1d, (double) oxygenTicksLeft / (double) oxygenMaxTicks));
        int filledW = (int)Math.round(barWidth * frac);
        if (filledW < 0) filledW = 0;
        if (filledW > barWidth) filledW = barWidth;

        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        gg.blit(OXY_BG,x, y,barWidth, barHeight,0f, 0f,TEX_W, TEX_H,TEX_W, TEX_H);

        if (filledW > 0) {
            int capDst = Math.max(1, (int)Math.round(CAP_SRC_PX * (barHeight / (double)TEX_H)));
            int maxCaps = Math.min(capDst * 2, filledW);
            int leftCapW = Math.min(capDst, filledW);
            gg.blit(
                    OXY_FILL,
                    x, y,
                    leftCapW, barHeight,
                    0f, 0f,
                    CAP_SRC_PX, TEX_H,
                    TEX_W, TEX_H
            );

            if (filledW > capDst) {
                int midSrcW = TEX_W - (2 * CAP_SRC_PX);
                int midDstW = filledW - Math.min(maxCaps, capDst);

                if (midDstW > 0) {
                    gg.blit(
                            OXY_FILL,
                            x + capDst, y,
                            midDstW, barHeight,
                            CAP_SRC_PX, 0f,
                            midSrcW, TEX_H,
                            TEX_W, TEX_H
                    );
                }

                if (filledW >= (capDst * 2)) {
                    gg.blit(
                            OXY_FILL,
                            x + filledW - capDst, y,
                            capDst, barHeight,
                            TEX_W - CAP_SRC_PX, 0f,
                            CAP_SRC_PX, TEX_H,
                            TEX_W, TEX_H
                    );
                }
            }
        }


        var font = Minecraft.getInstance().font;
        var comp = computeOxygenETA();
        int tx = x + barWidth / 2 - font.width(comp) / 2;
        int ty = y + (barHeight - 8) / 2 +1;
        gg.drawString(font, comp, tx, ty, 0xFFFFFF, true);

        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }




    private static Component computeOxygenETA() {
        if (oxygenTicksLeft <= 0) return Component.literal("SUFFOCATING").withStyle(s -> s.withColor(COLOR_DRAIN));
        if (oxygenTicksLeft >= oxygenMaxTicks) return Component.literal("--:--").withStyle(s -> s.withColor(COLOR_IDLE));

        double r = lastRateTicksPerSecond; // signed ticks/sec from server
        if (!Double.isFinite(r) || Math.abs(r) < 0.01) {
            return Component.literal("--:--").withStyle(s -> s.withColor(COLOR_IDLE));
        }

        if (r < 0) {
            long etaSec = (long)Math.ceil(oxygenTicksLeft / (-r));
            return Component.literal("<- " + formatSeconds(etaSec)  + "  >").withStyle(s -> s.withColor(COLOR_DRAIN));
        } else {
            long ticksNeeded = oxygenMaxTicks - oxygenTicksLeft;
            long etaSec = (long)Math.ceil(ticksNeeded / r);
            return Component.literal("<  " + formatSeconds(etaSec)  + " ->").withStyle(s -> s.withColor(COLOR_REGEN));
        }
    }



    private static String formatSeconds(long sec) {
        if (sec < 0) sec = 0;
        long m = sec / 60;
        long s = sec % 60;
        return m + ":" + String.format("%02d", s);
    }


}
