package com.ghostipedia.cosmiccore.client.gui.widget.starladder;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;

import com.lowdragmc.lowdraglib.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.IntSupplier;

import javax.annotation.Nonnull;

public class StarLadderFancyUIWidget extends FancyMachineUIWidget {

    private final IntSupplier tierSupplier;

    private static final int BG_COLOR = 0xE0080812;
    private static final int BORDER_COLOR = 0xFF304060;
    private static final int SLOT_BG_COLOR = 0xC0101018;
    private static final int SLOT_BORDER_COLOR = 0xFF405070;

    private static final int[] TIER_COLORS = {
            0xFF4080C0, // T0 - Blue
            0xFF40C080, // T1 - Green
            0xFFC0A040, // T2 - Gold
            0xFFC040C0  // T3 - Purple
    };

    public StarLadderFancyUIWidget(IFancyUIProvider mainPage, int width, int height, IntSupplier tierSupplier) {
        super(mainPage, width, height);
        this.tierSupplier = tierSupplier;
        setBackground((IGuiTexture) null);
        applyDarkTheme();
    }

    private void applyDarkTheme() {
        IGuiTexture titleBarBg = new GuiTextureGroup(
                new ColorRectTexture(BG_COLOR),
                new ColorBorderTexture(1, BORDER_COLOR));

        if (titleBar != null) {
            titleBar.setBackground((IGuiTexture) null);
            for (Widget widget : titleBar.widgets) {
                if (widget instanceof WidgetGroup group) {
                    group.setBackground(titleBarBg);
                }
            }
        }

        if (sideTabsWidget != null) {
            sideTabsWidget.setBackground((IGuiTexture) null);
            updateTabStyling();
        }

        if (configuratorPanel != null) {
            configuratorPanel.setVisible(false);
            configuratorPanel.setActive(false);
        }

        applySlotStyling();
    }

    private void applySlotStyling() {
        if (playerInventory != null) {
            playerInventory.setBackground((IGuiTexture) null);
            IGuiTexture darkSlot = new GuiTextureGroup(
                    new ColorRectTexture(SLOT_BG_COLOR),
                    new ColorBorderTexture(1, SLOT_BORDER_COLOR));
            for (Widget widget : playerInventory.widgets) {
                if (widget instanceof SlotWidget slotWidget) {
                    slotWidget.setBackground(darkSlot);
                }
            }
        }
    }

    private void updateTabStyling() {
        if (sideTabsWidget == null) return;

        int tier = tierSupplier != null ? tierSupplier.getAsInt() : 0;
        int accentColor = getTierColor(tier);
        int accentColorDim = dimColor(accentColor, 0.6f);

        IGuiTexture tabNormal = new GuiTextureGroup(
                new ColorRectTexture(0xA0080812),
                new ColorBorderTexture(1, accentColorDim));
        IGuiTexture tabHover = new GuiTextureGroup(
                new ColorRectTexture(0xC0101828),
                new ColorBorderTexture(1, accentColor));
        IGuiTexture tabPressed = new GuiTextureGroup(
                new ColorRectTexture(0xE0081020),
                new ColorBorderTexture(1, accentColor));

        sideTabsWidget.setTabTexture(tabNormal);
        sideTabsWidget.setTabHoverTexture(tabHover);
        sideTabsWidget.setTabPressedTexture(tabPressed);
    }

    private int getTierColor(int tier) {
        if (tier < 0) return TIER_COLORS[0];
        if (tier >= TIER_COLORS.length) return TIER_COLORS[TIER_COLORS.length - 1];
        return TIER_COLORS[tier];
    }

    private int dimColor(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int) (((color >> 16) & 0xFF) * factor);
        int g = (int) (((color >> 8) & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public void initWidget() {
        super.initWidget();
        applySlotStyling();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        updateTabStyling();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawFullBackground(graphics);
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        drawTitleText(graphics);
    }

    private void drawFullBackground(GuiGraphics graphics) {
        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;
        int tier = tierSupplier != null ? tierSupplier.getAsInt() : 0;
        int tierColor = getTierColor(tier);

        DrawerHelper.drawGradientRect(graphics, x, y, w, h, 0xFF080c14, 0xFF040608, false);
        drawGridPattern(graphics, x, y, w, h);
        drawCornerAccents(graphics, x, y, w, h, (tierColor & 0x00FFFFFF) | 0x40000000);
        DrawerHelper.drawBorder(graphics, x, y, w, h, (tierColor & 0x00FFFFFF) | 0x30000000, 1);

        if (playerInventory != null && playerInventory.isVisible()) {
            int invY = playerInventory.getPosition().y;
            int accentColor = (tierColor & 0x00FFFFFF) | 0x60000000;
            graphics.fill(x + 10, invY - 2, x + w - 10, invY - 1, accentColor);
        }
    }

    private void drawGridPattern(GuiGraphics graphics, int x, int y, int w, int h) {
        int gridColor = 0x0A4080FF;
        int spacing = 20;
        for (int gx = x + spacing; gx < x + w; gx += spacing) {
            graphics.fill(gx, y, gx + 1, y + h, gridColor);
        }
        for (int gy = y + spacing; gy < y + h; gy += spacing) {
            graphics.fill(x, gy, x + w, gy + 1, gridColor);
        }
    }

    private void drawCornerAccents(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        int len = 16;
        int thickness = 2;
        graphics.fill(x, y, x + len, y + thickness, color);
        graphics.fill(x, y, x + thickness, y + len, color);
        graphics.fill(x + w - len, y, x + w, y + thickness, color);
        graphics.fill(x + w - thickness, y, x + w, y + len, color);
        graphics.fill(x, y + h - thickness, x + len, y + h, color);
        graphics.fill(x, y + h - len, x + thickness, y + h, color);
        graphics.fill(x + w - len, y + h - thickness, x + w, y + h, color);
        graphics.fill(x + w - thickness, y + h - len, x + w, y + h, color);
    }

    private void drawTitleText(GuiGraphics graphics) {
        if (titleBar == null || mainPage == null) return;

        var font = Minecraft.getInstance().font;
        String title = mainPage.getTitle().getString();

        int titleBarX = getPosition().x + 8;
        int titleBarY = getPosition().y - 16;
        int textAreaX = titleBarX + 18 + 16;
        int textAreaY = titleBarY + 3;
        int textAreaWidth = getSize().width - 16 - 18 - 18 - 16;
        int textAreaHeight = 13;

        graphics.fill(textAreaX, textAreaY, textAreaX + textAreaWidth, textAreaY + textAreaHeight, BG_COLOR);

        int textWidth = font.width(title);
        int centeredX = textAreaX + (textAreaWidth - textWidth) / 2;
        int centeredY = textAreaY + (textAreaHeight - font.lineHeight) / 2;

        int tier = tierSupplier != null ? tierSupplier.getAsInt() : 0;
        int titleColor = getTierColor(tier);
        graphics.drawString(font, title, centeredX, centeredY, titleColor, true);
    }
}
