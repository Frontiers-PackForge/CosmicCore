package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine;
import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine.Stage;

import com.gregtechceu.gtceu.api.GTValues;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

public class ModuleConfigPopout extends WidgetGroup {

    public static final int WIDTH = 170;
    public static final int HEIGHT = 140;
    private static final int TITLE_HEIGHT = 16;
    private static final int SETTINGS_BUTTON_SIZE = 24;
    private static final ResourceLocation GEAR_TEXTURE = ResourceLocation.fromNamespaceAndPath("gtceu",
            "textures/item/material_sets/dull/gear_small.png");

    private final Supplier<IrisMultiblockMachine> machineSupplier;
    private final Runnable onClose;

    private int moduleIndex = -1;
    private boolean dragging = false;
    private double lastDeltaX, lastDeltaY;

    private String moduleName = "";
    private boolean moduleConnected = false;
    private boolean moduleWorking = false;
    private long energyPerTick = 0;
    private double speedBonus = 0;
    private Stage irisStage = Stage.EMPTY;

    private int configuredMaxParallel = 1;
    private long configuredVoltage = 32;
    private int irisParallelLimit = 1;

    private long maxEUt = 0;
    private int effectiveParallel = 1;
    private int overclockTier = 0;

    private PowerControlPopup powerPopup;
    private boolean showingPowerPopup = false;
    private java.util.function.BiConsumer<Integer, Long> onPowerSettingsChanged;

    private float pulsePhase = 0f;
    private float appearProgress = 0f;

    public ModuleConfigPopout(int x, int y, Supplier<IrisMultiblockMachine> machineSupplier, Runnable onClose) {
        super(x, y, WIDTH, HEIGHT);
        this.machineSupplier = machineSupplier;
        this.onClose = onClose;
        setVisible(false);
        initPowerPopup();
    }

    private void initPowerPopup() {
        powerPopup = new PowerControlPopup(WIDTH + 10, 0, this::hidePowerPopup, this::onPowerSettingsApplied);
        addWidget(powerPopup);
    }

    public void setOnPowerSettingsChanged(java.util.function.BiConsumer<Integer, Long> callback) {
        this.onPowerSettingsChanged = callback;
    }

    private void showPowerPopup() {
        showingPowerPopup = true;
        powerPopup.show(configuredMaxParallel, configuredVoltage);
    }

    private void hidePowerPopup() {
        showingPowerPopup = false;
        powerPopup.hide();
    }

    private void onPowerSettingsApplied(PowerControlPopup.PowerSettings settings) {
        this.configuredMaxParallel = settings.maxParallel();
        this.configuredVoltage = settings.voltagePerParallel();

        if (onPowerSettingsChanged != null) {
            onPowerSettingsChanged.accept(configuredMaxParallel, configuredVoltage);
        }
    }

    public void showForModule(int index) {
        this.moduleIndex = index;
        this.appearProgress = 0f;
        setVisible(true);
        setActive(true);
    }

    public void hide() {
        setVisible(false);
        setActive(false);
        moduleIndex = -1;
        hidePowerPopup();
    }

    @OnlyIn(Dist.CLIENT)
    public void updateModuleData(String name, boolean connected, boolean working, long energy, double speed,
                                 Stage stage,
                                 int maxParallel, long voltage, int irisLimit,
                                 long moduleMaxEUt, int moduleEffectiveParallel, int moduleOverclockTier) {
        this.moduleName = name;
        this.moduleConnected = connected;
        this.moduleWorking = working;
        this.energyPerTick = energy;
        this.speedBonus = speed;
        this.irisStage = stage;
        this.configuredMaxParallel = maxParallel;
        this.configuredVoltage = voltage;
        this.irisParallelLimit = irisLimit;
        this.maxEUt = moduleMaxEUt;
        this.effectiveParallel = moduleEffectiveParallel;
        this.overclockTier = moduleOverclockTier;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        pulsePhase += 0.1f;

        if (isVisible() && appearProgress < 1f) {
            appearProgress = Math.min(1f, appearProgress + 0.15f);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!isVisible()) return;

        float alpha = appearProgress;
        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        int bgAlpha = (int) (0xE0 * alpha);
        int bgColor = (bgAlpha << 24) | 0x0c0c14;
        DrawerHelper.drawSolidRect(graphics, x, y, w, h, bgColor);

        int gridAlpha = (int) (0x08 * alpha);
        int gridColor = (gridAlpha << 24) | 0xFFFFFF;
        for (int gx = x + 8; gx < x + w; gx += 8) {
            graphics.fill(gx, y, gx + 1, y + h, gridColor);
        }
        for (int gy = y + 8; gy < y + h; gy += 8) {
            graphics.fill(x, gy, x + w, gy + 1, gridColor);
        }

        int titleBgAlpha = (int) (0xC0 * alpha);
        int titleBgColor = (titleBgAlpha << 24) | 0x101820;
        DrawerHelper.drawSolidRect(graphics, x, y, w, TITLE_HEIGHT, titleBgColor);

        int accentColor = getAccentColor();
        int accentAlpha = (int) (0x80 * alpha);
        int borderColor = (accentAlpha << 24) | (accentColor & 0x00FFFFFF);

        DrawerHelper.drawBorder(graphics, x, y, w, h, borderColor, 1);
        graphics.fill(x + 1, y + TITLE_HEIGHT - 2, x + w - 1, y + TITLE_HEIGHT, borderColor);

        drawTitle(graphics, x, y, w, alpha);
        drawContent(graphics, x, y + TITLE_HEIGHT + 4, w, alpha);
        drawSettingsButton(graphics, x + w - SETTINGS_BUTTON_SIZE - 4, y + h - SETTINGS_BUTTON_SIZE - 4, mouseX, mouseY,
                alpha);
        drawCloseButton(graphics, x + w - 14, y + 3, mouseX, mouseY, alpha);

        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
    }

    private void drawTitle(GuiGraphics graphics, int x, int y, int w, float alpha) {
        var font = Minecraft.getInstance().font;

        String title = moduleName.isEmpty() ? Component.translatable("cosmiccore.stellar.module.config").getString() :
                Component.translatable(moduleName).getString();
        int maxWidth = w - 24;
        if (font.width(title) > maxWidth) {
            while (font.width(title + "...") > maxWidth && title.length() > 1) {
                title = title.substring(0, title.length() - 1);
            }
            title = title + "...";
        }

        int textColor = (int) (0xFF * alpha) << 24 | 0xFFFFFF;
        graphics.drawString(font, title, x + 4, y + (TITLE_HEIGHT - font.lineHeight) / 2 + 1, textColor, false);
    }

    private void drawContent(GuiGraphics graphics, int x, int y, int w, float alpha) {
        var font = Minecraft.getInstance().font;
        int labelColor = (int) (0xFF * alpha) << 24 | 0x808090;
        int valueColor = (int) (0xFF * alpha) << 24 | 0xDDDDDD;
        int accentColor = (int) (0xFF * alpha) << 24 | 0x80C0FF;

        int lineHeight = 11;
        int contentX = x + 6;
        int valueX = x + 70;
        int currentY = y;

        String statusValue;
        int statusColor;
        if (moduleWorking) {
            statusValue = Component.translatable("cosmiccore.stellar.module.status.processing").getString();
            statusColor = 0x44FF44;
        } else if (moduleConnected) {
            statusValue = Component.translatable("cosmiccore.stellar.module.status.idle").getString();
            statusColor = 0x6090CC;
        } else {
            statusValue = Component.translatable("cosmiccore.stellar.module.status.offline").getString();
            statusColor = 0xFF5555;
        }
        statusColor = (int) (0xFF * alpha) << 24 | statusColor;

        graphics.drawString(font, Component.translatable("cosmiccore.stellar.module.status").getString(), contentX,
                currentY, labelColor, false);
        graphics.drawString(font, statusValue, valueX, currentY, statusColor, false);
        currentY += lineHeight;

        int sepAlpha = (int) (0x30 * alpha);
        graphics.fill(contentX, currentY, x + w - 6, currentY + 1, (sepAlpha << 24) | 0x4080FF);
        currentY += 4;

        graphics.drawString(font, Component.translatable("cosmiccore.stellar.module.max_eut").getString(), contentX,
                currentY, labelColor, false);
        String maxEUtStr = formatEnergy(maxEUt);
        graphics.drawString(font, maxEUtStr, valueX, currentY, valueColor, false);

        String tierName = overclockTier < GTValues.VNF.length ? GTValues.VNF[overclockTier] : "MAX";
        int tierColor = getTierColor(overclockTier);
        int badgeX = x + w - 6 - font.width(tierName) - 4;
        int badgeAlpha = (int) (0x90 * alpha);
        graphics.fill(badgeX - 2, currentY - 1, badgeX + font.width(tierName) + 2, currentY + font.lineHeight,
                (badgeAlpha << 24) | (tierColor & 0x00333333));
        graphics.drawString(font, tierName, badgeX, currentY, (int) (0xFF * alpha) << 24 | tierColor, false);
        currentY += lineHeight;

        graphics.drawString(font, Component.translatable("cosmiccore.stellar.module.parallel").getString(), contentX,
                currentY, labelColor, false);
        String parallelStr = effectiveParallel + "x";
        if (effectiveParallel < configuredMaxParallel) {
            parallelStr = Component
                    .translatable("cosmiccore.stellar.module.parallel_max", effectiveParallel, configuredMaxParallel)
                    .getString();
        }
        graphics.drawString(font, parallelStr, valueX, currentY, accentColor, false);
        currentY += lineHeight;

        graphics.drawString(font, Component.translatable("cosmiccore.stellar.module.current").getString(), contentX,
                currentY, labelColor, false);
        if (energyPerTick > 0) {
            String currentEUStr = formatEnergy(energyPerTick);
            graphics.drawString(font, currentEUStr, valueX, currentY, (int) (0xFF * alpha) << 24 | 0xFFCC44, false);
        } else {
            graphics.drawString(font, "---", valueX, currentY, (int) (0x80 * alpha) << 24 | 0x606060, false);
        }
        currentY += lineHeight;

        graphics.fill(contentX, currentY, x + w - 6, currentY + 1, (sepAlpha << 24) | 0x4080FF);
        currentY += 4;

        if (moduleConnected) {
            graphics.drawString(font, Component.translatable("cosmiccore.stellar.module.speed_bonus").getString(),
                    contentX, currentY, labelColor, false);
            String speedStr = speedBonus > 0 ? String.format("%.1fx", speedBonus) : "1.0x";
            int speedColor = speedBonus > 1.0 ? 0x66FF66 : 0xCCCCCC;
            graphics.drawString(font, speedStr, valueX, currentY, (int) (0xFF * alpha) << 24 | speedColor, false);
            currentY += lineHeight;

            graphics.drawString(font, Component.translatable("cosmiccore.stellar.module.iris_limit").getString(),
                    contentX, currentY, labelColor, false);
            graphics.drawString(font, irisParallelLimit + "x", valueX, currentY, valueColor, false);
        } else {
            int disconnectedColor = (int) (0x80 * alpha) << 24 | 0x808080;
            graphics.drawString(font, Component.translatable("cosmiccore.stellar.module.not_linked").getString(),
                    contentX, currentY, disconnectedColor, false);
        }

        if (moduleWorking) {
            int barY = y + HEIGHT - TITLE_HEIGHT - 14;
            int barWidth = w - 12;
            float progress = (float) (0.5f + 0.5f * Math.sin(System.currentTimeMillis() / 400.0));
            int fillWidth = (int) (barWidth * progress);

            int barBg = (int) (0x40 * alpha) << 24 | 0x000000;
            int barFill = (int) (0xC0 * alpha) << 24 | 0x44FF44;

            graphics.fill(contentX, barY, contentX + barWidth, barY + 3, barBg);
            graphics.fill(contentX, barY, contentX + fillWidth, barY + 3, barFill);
        }
    }

    private int getTierColor(int tier) {
        return switch (tier) {
            case 0 -> 0x808080;
            case 1 -> 0xC0C0C0;
            case 2 -> 0x00FFFF;
            case 3 -> 0xFFFF00;
            case 4 -> 0x0080FF;
            case 5 -> 0x8000FF;
            case 6 -> 0xFF0080;
            case 7 -> 0xFF00FF;
            case 8 -> 0x00FF00;
            default -> 0xFF4040;
        };
    }

    private void drawSettingsButton(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, float alpha) {
        int size = SETTINGS_BUTTON_SIZE;
        boolean hovered = mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size;

        int bgColor = hovered ? (int) (0xC0 * alpha) << 24 | 0x4080FF : (int) (0x60 * alpha) << 24 | 0x404050;

        graphics.fill(x, y, x + size, y + size, bgColor);
        graphics.blit(GEAR_TEXTURE, x, y, 0, 0, size, size, size, size);
    }

    private void drawCloseButton(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, float alpha) {
        int size = 10;
        boolean hovered = mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size;

        int bgColor = hovered ? (int) (0xC0 * alpha) << 24 | 0xFF4444 : (int) (0x60 * alpha) << 24 | 0x404050;
        int fgColor = (int) (0xFF * alpha) << 24 | 0xFFFFFF;

        graphics.fill(x, y, x + size, y + size, bgColor);
        graphics.fill(x + 2, y + 3, x + 4, y + 7, fgColor);
        graphics.fill(x + 6, y + 3, x + 8, y + 7, fgColor);
        graphics.fill(x + 3, y + 4, x + 7, y + 6, fgColor);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isVisible()) return false;

        if (showingPowerPopup && powerPopup.isVisible()) {
            if (powerPopup.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        int settingsX = x + w - SETTINGS_BUTTON_SIZE - 4;
        int settingsY = y + h - SETTINGS_BUTTON_SIZE - 4;
        if (mouseX >= settingsX && mouseX < settingsX + SETTINGS_BUTTON_SIZE && mouseY >= settingsY &&
                mouseY < settingsY + SETTINGS_BUTTON_SIZE) {
            if (showingPowerPopup) {
                hidePowerPopup();
            } else {
                showPowerPopup();
            }
            playButtonClickSound();
            return true;
        }

        int closeX = x + w - 14;
        int closeY = y + 3;
        if (mouseX >= closeX && mouseX < closeX + 10 && mouseY >= closeY && mouseY < closeY + 10) {
            if (onClose != null) {
                onClose.run();
            }
            hide();
            playButtonClickSound();
            return true;
        }

        if (mouseX >= x && mouseX < x + w - 30 && mouseY >= y && mouseY < y + TITLE_HEIGHT) {
            dragging = true;
            lastDeltaX = 0;
            lastDeltaY = 0;
            return true;
        }

        if (isMouseOverElement(mouseX, mouseY)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        double dx = dragX + lastDeltaX;
        double dy = dragY + lastDeltaY;
        int intDx = (int) dx;
        int intDy = (int) dy;
        lastDeltaX = dx - intDx;
        lastDeltaY = dy - intDy;

        if (dragging) {
            addSelfPosition(intDx, intDy);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging) {
            dragging = false;
            lastDeltaX = 0;
            lastDeltaY = 0;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private String formatEnergy(long eu) {
        if (eu >= 1_000_000_000) return String.format("%.1fG EU/t", eu / 1_000_000_000.0);
        if (eu >= 1_000_000) return String.format("%.1fM EU/t", eu / 1_000_000.0);
        if (eu >= 1000) return String.format("%.1fk EU/t", eu / 1000.0);
        return String.format("%d EU/t", eu);
    }

    private int getAccentColor() {
        if (moduleConnected) {
            return getStageColorRaw(irisStage);
        }
        return 0x4080AA;
    }

    private int getStageColorRaw(Stage stage) {
        return switch (stage) {
            case EMPTY -> 0x606060;
            case GROWING -> 0x66AAFF;
            case STAR -> 0xFFCC44;
            case SUPERSTAR -> 0xFF8844;
            case BLACK_HOLE -> 0xAA66FF;
            case DEATH -> 0xFF4444;
            case DEATH_GRACEFUL -> 0x886666;
        };
    }

    public int getModuleIndex() {
        return moduleIndex;
    }
}
