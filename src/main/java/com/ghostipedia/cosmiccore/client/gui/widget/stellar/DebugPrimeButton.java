package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class DebugPrimeButton extends Widget {

    private final Runnable onClick;
    private boolean hovered = false;

    public DebugPrimeButton(int x, int y, int width, int height, Runnable onClick) {
        super(x, y, width, height);
        this.onClick = onClick;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);

        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        hovered = isMouseOverElement(mouseX, mouseY);

        // Background - yellow/gold tint for "prime"
        int bgColor = hovered ? 0xAA404020 : 0x80302010;
        DrawerHelper.drawSolidRect(graphics, x, y, w, h, bgColor);

        // Border - golden when hovered
        int borderColor = hovered ? 0xFFFFCC44 : 0xFF806020;
        DrawerHelper.drawBorder(graphics, x, y, w, h, borderColor, 1);

        // Label
        var font = Minecraft.getInstance().font;
        String label = "[PRIME]";
        int labelWidth = font.width(label);
        int labelX = x + (w - labelWidth) / 2;
        int labelY = y + (h - font.lineHeight) / 2 + 1;

        int textColor = hovered ? 0xFFFFDD66 : 0xFFAA9944;
        graphics.drawString(font, label, labelX, labelY, textColor, false);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOverElement(mouseX, mouseY)) {
            onClick.run();
            return true;
        }
        return false;
    }
}
