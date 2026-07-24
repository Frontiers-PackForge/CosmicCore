package com.ghostipedia.cosmiccore.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public final class CompactAmountRenderer {

    private static final float SCALE = 0.5f;

    private CompactAmountRenderer() {}

    public static void drawFluidAmount(GuiGraphics graphics, int x, int y, int width, int height, long amount) {
        drawAmount(graphics, x, y, width, height, formatFluidAmount(amount));
    }

    public static void drawItemAmount(GuiGraphics graphics, int x, int y, int width, int height, long amount) {
        drawAmount(graphics, x, y, width, height, formatItemAmount(amount));
    }

    private static void drawAmount(GuiGraphics graphics, int x, int y, int width, int height, String text) {
        Minecraft client = Minecraft.getInstance();
        Component component = Component.literal(text);
        int textWidth = client.font.width(component);

        graphics.pose().pushPose();
        graphics.pose().translate(x + width, y + height, 200);
        graphics.pose().scale(SCALE, SCALE, 1);
        graphics.drawString(client.font, component, -textWidth, -client.font.lineHeight, 0xFFFFFF, true);
        graphics.pose().popPose();
    }

    private static String formatFluidAmount(long mB) {
        if (mB < 1000) return mB + "mB";
        double buckets = mB / 1000.0;
        if (buckets >= 1_000_000_000) return String.format(Locale.ROOT, "%.1fBB", buckets / 1_000_000_000);
        if (buckets >= 1_000_000) return String.format(Locale.ROOT, "%.1fMB", buckets / 1_000_000);
        if (buckets >= 1000) return String.format(Locale.ROOT, "%.1fKB", buckets / 1000);
        return String.format(Locale.ROOT, "%.1fB", buckets);
    }

    private static String formatItemAmount(long count) {
        if (count >= 1_000_000_000) return String.format(Locale.ROOT, "%.1fB", count / 1_000_000_000.0);
        if (count >= 1_000_000) return String.format(Locale.ROOT, "%.1fM", count / 1_000_000.0);
        if (count >= 1000) return String.format(Locale.ROOT, "%.1fK", count / 1000.0);
        return String.valueOf(count);
    }
}
