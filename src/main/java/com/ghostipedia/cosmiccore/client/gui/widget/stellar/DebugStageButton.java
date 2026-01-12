package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine.Stage;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class DebugStageButton extends Widget {

    private final Stage stage;
    private final Consumer<Stage> onClick;
    private boolean hovered = false;

    public DebugStageButton(int x, int y, int width, int height, Stage stage, Consumer<Stage> onClick) {
        super(x, y, width, height);
        this.stage = stage;
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

        // Background
        int bgColor = hovered ? 0xAA303040 : 0x80202030;
        DrawerHelper.drawSolidRect(graphics, x, y, w, h, bgColor);

        // Border with stage color
        int borderColor = getStageColor();
        if (hovered) {
            borderColor = brighten(borderColor);
        }
        DrawerHelper.drawBorder(graphics, x, y, w, h, borderColor, 1);

        // Label
        var font = Minecraft.getInstance().font;
        String label = getShortLabel();
        int labelWidth = font.width(label);
        int labelX = x + (w - labelWidth) / 2;
        int labelY = y + (h - font.lineHeight) / 2 + 1;

        int textColor = hovered ? 0xFFFFFFFF : 0xFFAAAAAA;
        graphics.drawString(font, label, labelX, labelY, textColor, false);
    }

    private String getShortLabel() {
        return switch (stage) {
            case EMPTY -> "EMP";
            case GROWING -> "GRW";
            case STAR -> "STR";
            case SUPERSTAR -> "SUP";
            case BLACK_HOLE -> "BLK";
            case DEATH -> "DTH";
            case DEATH_GRACEFUL -> "GRC";
        };
    }

    private int getStageColor() {
        return switch (stage) {
            case EMPTY -> 0xFF404050;
            case GROWING -> 0xFF6080FF;
            case STAR -> 0xFFFFCC44;
            case SUPERSTAR -> 0xFFFF8844;
            case BLACK_HOLE -> 0xFF8040FF;
            case DEATH -> 0xFFFF2020;
            case DEATH_GRACEFUL -> 0xFF804040;
        };
    }

    private int brighten(int color) {
        int a = (color >> 24) & 0xFF;
        int r = Math.min(255, ((color >> 16) & 0xFF) + 40);
        int g = Math.min(255, ((color >> 8) & 0xFF) + 40);
        int b = Math.min(255, (color & 0xFF) + 40);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOverElement(mouseX, mouseY)) {
            onClick.accept(stage);
            return true;
        }
        return false;
    }
}
