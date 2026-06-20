package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

import javax.annotation.Nonnull;

public class StarColorButton extends Widget {

    private final Consumer<Boolean> onToggle;
    private final IntSupplier colorSupplier;
    private boolean hovered = false;
    private float hoverProgress = 0f;
    private float pulsePhase = 0f;

    public StarColorButton(int x, int y, int width, int height, Consumer<Boolean> onToggle, IntSupplier colorSupplier) {
        super(x, y, width, height);
        this.onToggle = onToggle;
        this.colorSupplier = colorSupplier;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        pulsePhase += 0.1f;

        if (hovered && hoverProgress < 1f) {
            hoverProgress = Math.min(1f, hoverProgress + 0.15f);
        } else if (!hovered && hoverProgress > 0f) {
            hoverProgress = Math.max(0f, hoverProgress - 0.15f);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        hovered = isMouseOverElement(mouseX, mouseY);

        int bgAlpha = (int) (0xC0 + 0x20 * hoverProgress);
        int bgColor = (bgAlpha << 24) | 0x101820;
        DrawerHelper.drawSolidRect(graphics, x, y, w, h, bgColor);

        int currentColor = colorSupplier != null ? colorSupplier.getAsInt() : -1;
        int displayColor = currentColor == -1 ? 0xFFCC44 : currentColor;

        int borderAlpha = (int) (0x60 + 0x40 * hoverProgress);
        int borderColor = (borderAlpha << 24) | displayColor;
        DrawerHelper.drawBorder(graphics, x, y, w, h, borderColor, 1);

        drawColorIcon(graphics, x, y, w, h, displayColor, currentColor == -1);

        if (hoverProgress > 0) {
            int glowAlpha = (int) (0x20 * hoverProgress);
            int glowColor = (glowAlpha << 24) | displayColor;
            DrawerHelper.drawBorder(graphics, x - 1, y - 1, w + 2, h + 2, glowColor, 1);
        }
    }

    private void drawColorIcon(GuiGraphics graphics, int x, int y, int w, int h, int color, boolean isDefault) {
        int padding = 3;
        int iconX = x + padding;
        int iconY = y + padding;
        int iconW = w - padding * 2;
        int iconH = h - padding * 2;

        float pulseAlpha = 0.8f + 0.2f * Mth.sin(pulsePhase);
        int alpha = (int) (0xFF * pulseAlpha);

        if (isDefault) {
            int checkSize = 3;
            for (int cy = 0; cy < iconH / checkSize; cy++) {
                for (int cx = 0; cx < iconW / checkSize; cx++) {
                    int checkColor = ((cx + cy) % 2 == 0) ? 0xFF303030 : 0xFF505050;
                    int checkX = iconX + cx * checkSize;
                    int checkY = iconY + cy * checkSize;
                    int checkW = Math.min(checkSize, iconX + iconW - checkX);
                    int checkH = Math.min(checkSize, iconY + iconH - checkY);
                    graphics.fill(checkX, checkY, checkX + checkW, checkY + checkH, checkColor);
                }
            }
        }

        graphics.fill(iconX, iconY, iconX + iconW, iconY + iconH, (alpha << 24) | color);

        int innerBorder = 0x40000000;
        graphics.fill(iconX, iconY, iconX + iconW, iconY + 1, innerBorder);
        graphics.fill(iconX, iconY, iconX + 1, iconY + iconH, innerBorder);

        if (isDefault) {
            var font = Minecraft.getInstance().font;
            int textColor = 0x80FFFFFF;
            graphics.drawString(font, "D", iconX + iconW - 6, iconY + iconH - 8, textColor, false);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOverElement(mouseX, mouseY)) {
            if (onToggle != null) {
                onToggle.accept(true);
            }
            playButtonClickSound();
            return true;
        }
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (isMouseOverElement(mouseX, mouseY)) {
            graphics.renderTooltip(Minecraft.getInstance().font,
                    List.of(Component.translatable("cosmiccore.gui.stellar.star_color")),
                    java.util.Optional.empty(), mouseX, mouseY);
        }
    }
}
