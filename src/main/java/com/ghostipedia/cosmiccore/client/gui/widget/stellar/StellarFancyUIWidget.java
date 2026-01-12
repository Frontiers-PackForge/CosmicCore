package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine.Stage;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;

import com.lowdragmc.lowdraglib.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class StellarFancyUIWidget extends FancyMachineUIWidget {

    private final Supplier<Stage> stageSupplier;
    private Stage lastStage = Stage.EMPTY;

    private static final int BG_COLOR = 0xE00a0a14;
    private static final int BORDER_COLOR = 0xFF404060;
    private static final int SLOT_BG_COLOR = 0xC0101018;
    private static final int SLOT_BORDER_COLOR = 0xFF505070;

    public StellarFancyUIWidget(IFancyUIProvider mainPage, int width, int height, Supplier<Stage> stageSupplier) {
        super(mainPage, width, height);
        this.stageSupplier = stageSupplier;
        setBackground((IGuiTexture) null);
        applyDarkTheme();
    }

    private void applyDarkTheme() {
        IGuiTexture titleBarBg = new GuiTextureGroup(
            new ColorRectTexture(BG_COLOR),
            new ColorBorderTexture(1, BORDER_COLOR)
        );

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
            updateTabStyling(Stage.EMPTY);
        }

        if (configuratorPanel != null) {
            configuratorPanel.setVisible(false);
            configuratorPanel.setActive(false);
        }

        if (playerInventory != null) {
            playerInventory.setBackground((IGuiTexture) null);
            IGuiTexture darkSlot = new GuiTextureGroup(
                new ColorRectTexture(SLOT_BG_COLOR),
                new ColorBorderTexture(1, SLOT_BORDER_COLOR)
            );
            for (Widget widget : playerInventory.widgets) {
                if (widget instanceof SlotWidget slotWidget) {
                    slotWidget.setBackground(darkSlot);
                }
            }
        }
    }

    private void updateTabStyling(Stage stage) {
        if (sideTabsWidget == null) return;

        int accentColor = getStageAccentColorFull(stage);
        int accentColorDim = dimColor(accentColor, 0.6f);

        IGuiTexture tabNormal = new GuiTextureGroup(
            new ColorRectTexture(0xA0080812),
            new ColorBorderTexture(1, accentColorDim)
        );
        IGuiTexture tabHover = new GuiTextureGroup(
            new ColorRectTexture(0xC0151525),
            new ColorBorderTexture(1, accentColor)
        );
        IGuiTexture tabPressed = new GuiTextureGroup(
            new ColorRectTexture(0xE0101020),
            new ColorBorderTexture(1, accentColor)
        );

        sideTabsWidget.setTabTexture(tabNormal);
        sideTabsWidget.setTabHoverTexture(tabHover);
        sideTabsWidget.setTabPressedTexture(tabPressed);
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
        if (playerInventory != null) {
            IGuiTexture darkSlot = new GuiTextureGroup(
                new ColorRectTexture(SLOT_BG_COLOR),
                new ColorBorderTexture(1, SLOT_BORDER_COLOR)
            );
            for (Widget widget : playerInventory.widgets) {
                if (widget instanceof SlotWidget slotWidget) {
                    slotWidget.setBackground(darkSlot);
                }
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        Stage currentStage = stageSupplier.get();
        if (currentStage != lastStage) {
            lastStage = currentStage;
            updateTabStyling(currentStage);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawCustomOverlays(graphics);
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        drawTitleText(graphics);
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
        graphics.drawString(font, title, centeredX, centeredY, 0xFFFFFFFF, true);
    }

    private void drawCustomOverlays(GuiGraphics graphics) {
        Stage stage = stageSupplier.get();
        int accentColor = getStageAccentColor(stage);

        if (playerInventory != null && playerInventory.isVisible()) {
            int x = getPosition().x;
            int w = getSize().width;
            int invY = playerInventory.getPosition().y;
            graphics.fill(x + 10, invY - 2, x + w - 10, invY - 1, accentColor);
        }
    }

    private int getStageAccentColor(Stage stage) {
        int alpha = 0x60;
        return (alpha << 24) | switch (stage) {
            case EMPTY -> 0x404060;
            case GROWING -> 0x6080FF;
            case STAR -> 0xFFCC44;
            case SUPERSTAR -> 0xFF8844;
            case BLACK_HOLE -> 0x8040FF;
            case DEATH -> 0xFF2020;
            case DEATH_GRACEFUL -> 0x804040;
        };
    }

    private int getStageAccentColorFull(Stage stage) {
        return 0xFF000000 | switch (stage) {
            case EMPTY -> 0x404060;
            case GROWING -> 0x6080FF;
            case STAR -> 0xFFCC44;
            case SUPERSTAR -> 0xFF8844;
            case BLACK_HOLE -> 0x8040FF;
            case DEATH -> 0xFF2020;
            case DEATH_GRACEFUL -> 0x804040;
        };
    }
}
