package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.ghostipedia.cosmiccore.api.machine.feature.IStellarIrisProvider;
import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine.Stage;
import com.ghostipedia.cosmiccore.api.machine.multiblock.StellarBaseModule;

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
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

public class StellarModuleUIWidget extends FancyMachineUIWidget {

    private final Supplier<StellarBaseModule> moduleSupplier;

    private static final int BG_COLOR = 0xE00a0a14;
    private static final int BORDER_COLOR = 0xFF404060;
    private static final int SLOT_BG_COLOR = 0xC0101018;
    private static final int SLOT_BORDER_COLOR = 0xFF505070;
    private static final int MODULE_ACCENT = 0xFF4080AA;
    private static final int MODULE_ACCENT_DIM = 0xFF305070;

    private int syncedMaxParallel = 1;
    private long syncedVoltage = 32;
    private int syncedIrisParallelLimit = 1;
    private StellarModuleContentWidget contentWidget;

    public StellarModuleUIWidget(IFancyUIProvider mainPage, int width, int height,
                                 Supplier<StellarBaseModule> moduleSupplier) {
        super(mainPage, width, height);
        this.moduleSupplier = moduleSupplier;
        setBackground((IGuiTexture) null);
        applyDarkTheme();
    }

    private void onPowerSettingsChanged(int maxParallel, long voltage) {
        writeClientAction(1, buf -> {
            buf.writeInt(maxParallel);
            buf.writeLong(voltage);
        });
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        if (id == 1) {
            int newParallel = buffer.readInt();
            long newVoltage = buffer.readLong();

            StellarBaseModule module = moduleSupplier.get();
            if (module != null) {
                module.setConfiguredMaxParallel(newParallel);
                module.setConfiguredVoltagePerParallel(newVoltage);

                // Mark dirty so it saves
                module.markDirty();

                com.ghostipedia.cosmiccore.CosmicCore.LOGGER.info(
                        "[StellarModuleUI] SERVER received power settings: parallel={}, voltage={}",
                        newParallel, newVoltage);
            }
        } else {
            super.handleClientAction(id, buffer);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        StellarBaseModule module = moduleSupplier.get();
        if (module != null) {
            int currentParallel = module.getConfiguredMaxParallel();
            long currentVoltage = module.getConfiguredVoltagePerParallel();
            int currentIrisLimit = module.getIrisParallelLimit();
            if (currentParallel != syncedMaxParallel ||
                    currentVoltage != syncedVoltage ||
                    currentIrisLimit != syncedIrisParallelLimit) {
                syncedMaxParallel = currentParallel;
                syncedVoltage = currentVoltage;
                syncedIrisParallelLimit = currentIrisLimit;
                writeUpdateInfo(201, buf -> {
                    buf.writeInt(syncedMaxParallel);
                    buf.writeLong(syncedVoltage);
                    buf.writeInt(syncedIrisParallelLimit);
                });
            }
        }
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);

        StellarBaseModule module = moduleSupplier.get();
        if (module != null) {
            buffer.writeInt(module.getConfiguredMaxParallel());
            buffer.writeLong(module.getConfiguredVoltagePerParallel());
            buffer.writeInt(module.getIrisParallelLimit());
        } else {
            buffer.writeInt(1);
            buffer.writeLong(32L);
            buffer.writeInt(1);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        syncedMaxParallel = buffer.readInt();
        syncedVoltage = buffer.readLong();
        syncedIrisParallelLimit = buffer.readInt();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 201) {
            syncedMaxParallel = buffer.readInt();
            syncedVoltage = buffer.readLong();
            syncedIrisParallelLimit = buffer.readInt();
        } else {
            super.readUpdateInfo(id, buffer);
        }
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

        int accentColor = getAccentColor();
        int accentColorDim = dimColor(accentColor, 0.6f);

        IGuiTexture tabNormal = new GuiTextureGroup(
                new ColorRectTexture(0xA0080812),
                new ColorBorderTexture(1, accentColorDim));
        IGuiTexture tabHover = new GuiTextureGroup(
                new ColorRectTexture(0xC0151525),
                new ColorBorderTexture(1, accentColor));
        IGuiTexture tabPressed = new GuiTextureGroup(
                new ColorRectTexture(0xE0101020),
                new ColorBorderTexture(1, accentColor));

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
                    new ColorBorderTexture(1, SLOT_BORDER_COLOR));
            for (Widget widget : playerInventory.widgets) {
                if (widget instanceof SlotWidget slotWidget) {
                    slotWidget.setBackground(darkSlot);
                }
            }
        }

        // Find and wire up the content widget for power settings callback
        findContentWidget(this);
        if (contentWidget != null) {
            contentWidget.setOnPowerSettingsChanged(this::onPowerSettingsChanged);
        }
    }

    private void findContentWidget(WidgetGroup group) {
        for (Widget widget : group.widgets) {
            if (widget instanceof StellarModuleContentWidget smcw) {
                contentWidget = smcw;
                return;
            }
            if (widget instanceof WidgetGroup wg) {
                findContentWidget(wg);
                if (contentWidget != null) return;
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawFullBackground(graphics);
        drawCustomOverlays(graphics);
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        drawTitleText(graphics);
    }

    private void drawFullBackground(GuiGraphics graphics) {
        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        // Dark gradient background
        DrawerHelper.drawGradientRect(graphics, x, y, w, h, 0xFF0c0c12, 0xFF060608, false);

        // Subtle grid pattern
        drawGridPattern(graphics, x, y, w, h);

        // Corner accents
        int accentColor = getAccentColor();
        drawCornerAccents(graphics, x, y, w, h, accentColor & 0x66FFFFFF);

        // Border
        DrawerHelper.drawBorder(graphics, x, y, w, h, accentColor & 0x33FFFFFF, 1);

        // Side panels if inventory is visible
        if (playerInventory != null && playerInventory.isVisible()) {
            drawSidePanels(graphics, x, y, w, h);
        }
    }

    private void drawGridPattern(GuiGraphics graphics, int x, int y, int w, int h) {
        int gridColor = 0x08FFFFFF;
        int spacing = 16;
        for (int gx = x + spacing; gx < x + w; gx += spacing) {
            graphics.fill(gx, y, gx + 1, y + h, gridColor);
        }
        for (int gy = y + spacing; gy < y + h; gy += spacing) {
            graphics.fill(x, gy, x + w, gy + 1, gridColor);
        }
    }

    private void drawCornerAccents(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        int len = 20;
        int thickness = 2;
        // Top-left
        graphics.fill(x, y, x + len, y + thickness, color);
        graphics.fill(x, y, x + thickness, y + len, color);
        // Top-right
        graphics.fill(x + w - len, y, x + w, y + thickness, color);
        graphics.fill(x + w - thickness, y, x + w, y + len, color);
        // Bottom-left
        graphics.fill(x, y + h - thickness, x + len, y + h, color);
        graphics.fill(x, y + h - len, x + thickness, y + h, color);
        // Bottom-right
        graphics.fill(x + w - len, y + h - thickness, x + w, y + h, color);
        graphics.fill(x + w - thickness, y + h - len, x + w, y + h, color);
    }

    private void drawSidePanels(GuiGraphics graphics, int x, int y, int w, int h) {
        int invY = playerInventory.getPosition().y;
        int invX = playerInventory.getPosition().x;
        int invW = playerInventory.getSize().width;
        int panelH = h - (invY - y) - 5;

        // Left panel - connection status
        int leftPanelX = x + 3;
        int leftPanelW = invX - leftPanelX - 3;
        if (leftPanelW > 20) {
            drawConnectionPanel(graphics, leftPanelX, invY, leftPanelW, panelH);
        }

        // Right panel - stats
        int rightPanelX = invX + invW + 3;
        int rightPanelW = (x + w) - rightPanelX - 3;
        if (rightPanelW > 20) {
            drawStatsPanel(graphics, rightPanelX, invY, rightPanelW, panelH);
        }
    }

    private void drawConnectionPanel(GuiGraphics graphics, int px, int py, int pw, int ph) {
        DrawerHelper.drawSolidRect(graphics, px, py, pw, ph, 0x40000000);
        int borderColor = getAccentColor() & 0x33FFFFFF;
        DrawerHelper.drawBorder(graphics, px, py, pw, ph, borderColor, 1);
        int accentColor = getAccentColor() & 0x80FFFFFF;
        graphics.fill(px + 1, py + 1, px + pw - 1, py + 3, accentColor);

        var font = Minecraft.getInstance().font;
        StellarBaseModule module = moduleSupplier.get();

        if (module != null) {
            IStellarIrisProvider iris = module.getStellarIris();
            boolean connected = iris != null && iris.isFormed();
            boolean canProcess = connected && iris.canProcess();

            String statusText = connected ? (canProcess ? "LINKED" : "WAITING") : "OFFLINE";
            int statusColor = connected ? (canProcess ? 0xFF66FF66 : 0xFFFFAA44) : 0xFFFF4444;

            graphics.drawString(font, "IRIS LINK", px + 4, py + 6, accentColor | 0xFF000000, false);
            graphics.drawString(font, statusText, px + 4, py + 18, statusColor, false);

            // Animated connection indicator
            if (connected) {
                int pulseAlpha = (int) (128 + 64 * Math.sin(System.currentTimeMillis() / 200.0));
                int pulseColor = (pulseAlpha << 24) | (statusColor & 0x00FFFFFF);
                int indicatorY = py + 30;
                int indicatorW = (int) ((pw - 10) * (0.5f + 0.5f * Math.sin(System.currentTimeMillis() / 500.0)));
                graphics.fill(px + 5, indicatorY, px + 5 + Math.max(5, indicatorW), indicatorY + 2, pulseColor);
            }
        }
    }

    private void drawStatsPanel(GuiGraphics graphics, int px, int py, int pw, int ph) {
        DrawerHelper.drawSolidRect(graphics, px, py, pw, ph, 0x40000000);
        int borderColor = getAccentColor() & 0x33FFFFFF;
        DrawerHelper.drawBorder(graphics, px, py, pw, ph, borderColor, 1);
        int accentColor = getAccentColor() & 0x80FFFFFF;
        graphics.fill(px + 1, py + 1, px + pw - 1, py + 3, accentColor);

        var font = Minecraft.getInstance().font;
        int labelColor = 0xFF606080;
        int valueColor = 0xFFCCCCCC;

        graphics.drawString(font, "MODULE", px + 4, py + 6, accentColor | 0xFF000000, false);

        StellarBaseModule module = moduleSupplier.get();
        if (module != null) {
            IStellarIrisProvider iris = module.getStellarIris();

            // Energy usage
            long euPerTick = module.getEnergyConsumedPerTick();
            graphics.drawString(font, "EU/t:", px + 4, py + 20, labelColor, false);
            graphics.drawString(font, formatEnergy(euPerTick), px + 30, py + 20, valueColor, false);

            // Iris bonuses (if connected)
            if (iris != null && iris.canProcess()) {
                graphics.drawString(font, "SPEED:", px + 4, py + 32, labelColor, false);
                graphics.drawString(font, String.format("%.1fx", iris.getSpeedBonus()), px + 38, py + 32, 0xFF66FF66,
                        false);

                graphics.drawString(font, "STAGE:", px + 4, py + 44, labelColor, false);
                Stage stage = iris.getStage();
                graphics.drawString(font, getShortStageName(stage), px + 38, py + 44, getStageColor(stage), false);
            } else {
                graphics.drawString(font, "---", px + 4, py + 32, 0xFF404040, false);
            }
        }
    }

    private String formatEnergy(long eu) {
        if (eu >= 1_000_000_000) return String.format("%.1fG", eu / 1_000_000_000.0);
        if (eu >= 1_000_000) return String.format("%.1fM", eu / 1_000_000.0);
        if (eu >= 1000) return String.format("%.1fk", eu / 1000.0);
        return String.format("%d", eu);
    }

    private String getShortStageName(Stage stage) {
        return switch (stage) {
            case EMPTY -> "NONE";
            case GROWING -> "GROW";
            case STAR -> "STAR";
            case SUPERSTAR -> "SUPER";
            case BLACK_HOLE -> "B.HOLE";
            case DEATH -> "DEATH";
            case DEATH_GRACEFUL -> "FADE";
        };
    }

    private int getStageColor(Stage stage) {
        return switch (stage) {
            case EMPTY -> 0xFF606060;
            case GROWING -> 0xFF66AAFF;
            case STAR -> 0xFFFFCC44;
            case SUPERSTAR -> 0xFFFF8844;
            case BLACK_HOLE -> 0xFFAA66FF;
            case DEATH -> 0xFFFF4444;
            case DEATH_GRACEFUL -> 0xFF886666;
        };
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
        int maxWidth = textAreaWidth - 4;
        if (textWidth > maxWidth) {
            String ellipsis = "...";
            int ellipsisWidth = font.width(ellipsis);
            while (textWidth + ellipsisWidth > maxWidth && title.length() > 1) {
                title = title.substring(0, title.length() - 1);
                textWidth = font.width(title);
            }
            title = title + ellipsis;
            textWidth = font.width(title);
        }

        int centeredX = textAreaX + (textAreaWidth - textWidth) / 2;
        int centeredY = textAreaY + (textAreaHeight - font.lineHeight) / 2;

        graphics.enableScissor(textAreaX, textAreaY, textAreaX + textAreaWidth, textAreaY + textAreaHeight);
        graphics.drawString(font, title, centeredX, centeredY, 0xFFFFFFFF, true);
        graphics.disableScissor();
    }

    private void drawCustomOverlays(GuiGraphics graphics) {
        int accentColor = getAccentColor() & 0x60FFFFFF;

        if (playerInventory != null && playerInventory.isVisible()) {
            int x = getPosition().x;
            int w = getSize().width;
            int invY = playerInventory.getPosition().y;
            graphics.fill(x + 10, invY - 2, x + w - 10, invY - 1, accentColor);
        }
    }

    private int getAccentColor() {
        StellarBaseModule module = moduleSupplier.get();
        if (module != null) {
            IStellarIrisProvider iris = module.getStellarIris();
            if (iris != null && iris.isFormed() && iris.canProcess()) {
                return getStageAccentColor(iris.getStage());
            }
        }
        return MODULE_ACCENT;
    }

    private int getStageAccentColor(Stage stage) {
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
