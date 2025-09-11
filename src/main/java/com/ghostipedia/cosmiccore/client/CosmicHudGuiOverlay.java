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

    public static void setTimeBar(ResourceLocation dim, long left, long max) {
        timeTicksLeft = left;
        timeMaxTicks = max;
    }

    @Override
    public void render(ForgeGui forgeGui, GuiGraphics guiGraphics, float partialTick, int screenWidth,
                       int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isWindowActive() && mc.level != null && !mc.options.renderDebug && !mc.options.hideGui) {
            renderHUDWirelessPDA(WirelessPDABehavior.CosmicCuriosUtils.getPDACurio(mc.player), guiGraphics);
            renderTimeBudgetBar(guiGraphics, screenWidth, screenHeight);
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
        int y = sh - 28; // above hotbar; tweak if it overlaps other overlays

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
}
