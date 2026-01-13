package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Consumer;

import javax.annotation.Nonnull;

public class PowerControlPopup extends WidgetGroup {

    public static final int WIDTH = 150;
    public static final int HEIGHT = 90;
    private static final int TITLE_HEIGHT = 16;
    private static final int FIELD_HEIGHT = 20;

    private final Runnable onClose;
    private final Consumer<PowerSettings> onApply;

    private int maxParallel = 1;
    private long voltagePerParallel = 32;

    private TextFieldWidget parallelField;
    private TextFieldWidget voltageField;

    private boolean dragging = false;
    private double lastDeltaX, lastDeltaY;
    private float appearProgress = 0f;

    public PowerControlPopup(int x, int y, Runnable onClose, Consumer<PowerSettings> onApply) {
        super(x, y, WIDTH, HEIGHT);
        this.onClose = onClose;
        this.onApply = onApply;
        setVisible(false);
        initFields();
    }

    private void initFields() {
        int fieldX = 6;
        int fieldWidth = WIDTH - 12;
        int labelY = TITLE_HEIGHT + 4;

        parallelField = new TextFieldWidget(fieldX, labelY + 12, fieldWidth - 12, FIELD_HEIGHT - 4,
                () -> String.valueOf(maxParallel),
                this::onParallelChanged);
        parallelField.setClientSideWidget();
        parallelField.setNumbersOnly(1, Integer.MAX_VALUE);
        parallelField.setMaxStringLength(10);
        parallelField.setBackground(new GuiTextureGroup(
                new ColorRectTexture(0xE0101018),
                new ColorBorderTexture(1, 0xFF404060)));
        addWidget(parallelField);

        int voltageY = labelY + FIELD_HEIGHT + 16;
        voltageField = new TextFieldWidget(fieldX, voltageY + 12, fieldWidth - 12, FIELD_HEIGHT - 4,
                () -> String.valueOf(voltagePerParallel),
                this::onVoltageChanged);
        voltageField.setClientSideWidget();
        voltageField.setNumbersOnly(1L, Long.MAX_VALUE);
        voltageField.setMaxStringLength(20);
        voltageField.setBackground(new GuiTextureGroup(
                new ColorRectTexture(0xE0101018),
                new ColorBorderTexture(1, 0xFF404060)));
        addWidget(voltageField);
    }

    public void show(int parallel, long voltage) {
        this.maxParallel = parallel;
        this.voltagePerParallel = voltage;
        this.appearProgress = 0f;
        setVisible(true);
        setActive(true);

        if (parallelField != null) {
            parallelField.setCurrentString(String.valueOf(parallel));
        }
        if (voltageField != null) {
            voltageField.setCurrentString(String.valueOf(voltage));
        }
    }

    public void hide() {
        setVisible(false);
        setActive(false);
    }

    private void onParallelChanged(String text) {
        try {
            int value = Integer.parseInt(text);
            maxParallel = Math.max(1, value);
            applySettings();
        } catch (NumberFormatException ignored) {}
    }

    private void onVoltageChanged(String text) {
        try {
            long value = Long.parseLong(text);
            voltagePerParallel = Math.max(1, value);
            applySettings();
        } catch (NumberFormatException ignored) {}
    }

    private void applySettings() {
        if (onApply != null) {
            onApply.accept(new PowerSettings(maxParallel, voltagePerParallel));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
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

        int bgAlpha = (int) (0xE8 * alpha);
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

        int titleBgAlpha = (int) (0xD0 * alpha);
        int titleBgColor = (titleBgAlpha << 24) | 0x101820;
        DrawerHelper.drawSolidRect(graphics, x, y, w, TITLE_HEIGHT, titleBgColor);

        int borderAlpha = (int) (0x80 * alpha);
        int borderColor = (borderAlpha << 24) | 0x4080FF;
        DrawerHelper.drawBorder(graphics, x, y, w, h, borderColor, 1);

        graphics.fill(x + 1, y + TITLE_HEIGHT - 2, x + w - 1, y + TITLE_HEIGHT, borderColor);

        drawTitle(graphics, x, y, w, alpha);
        drawLabels(graphics, x, y, alpha);
        drawCloseButton(graphics, x + w - 14, y + 3, mouseX, mouseY, alpha);
        drawTierIndicator(graphics, x, y, alpha);

        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
    }

    private void drawTitle(GuiGraphics graphics, int x, int y, int w, float alpha) {
        var font = Minecraft.getInstance().font;
        String title = Component.translatable("cosmiccore.stellar.power.title").getString();
        int textColor = (int) (0xFF * alpha) << 24 | 0xFFFFFF;
        graphics.drawString(font, title, x + 4, y + (TITLE_HEIGHT - font.lineHeight) / 2 + 1, textColor, false);
    }

    private void drawLabels(GuiGraphics graphics, int x, int y, float alpha) {
        var font = Minecraft.getInstance().font;
        int labelColor = (int) (0xFF * alpha) << 24 | 0xA0A0B0;

        int labelY = y + TITLE_HEIGHT + 4;
        graphics.drawString(font, Component.translatable("cosmiccore.stellar.power.max_parallel").getString(), x + 6,
                labelY, labelColor, false);

        int voltageY = labelY + FIELD_HEIGHT + 16;
        graphics.drawString(font, Component.translatable("cosmiccore.stellar.power.voltage_per_parallel").getString(),
                x + 6, voltageY, labelColor, false);
    }

    private void drawTierIndicator(GuiGraphics graphics, int x, int y, float alpha) {
        var font = Minecraft.getInstance().font;
        int tier = GTUtil.getTierByVoltage(voltagePerParallel);
        String tierName = GTValues.VNF[Math.min(tier, GTValues.VNF.length - 1)];

        int labelY = y + TITLE_HEIGHT + 4 + FIELD_HEIGHT + 16;
        int badgeX = x + WIDTH - 6 - font.width(tierName) - 4;

        int tierColor = getTierColor(tier);
        int badgeAlpha = (int) (0x80 * alpha);
        int badgeBgColor = (badgeAlpha << 24) | (tierColor & 0x00FFFFFF);

        graphics.fill(badgeX - 2, labelY - 1, badgeX + font.width(tierName) + 2, labelY + font.lineHeight + 1,
                badgeBgColor);
        int textColor = (int) (0xFF * alpha) << 24 | 0xFFFFFF;
        graphics.drawString(font, tierName, badgeX, labelY, textColor, false);
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

        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;

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

        if (mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + TITLE_HEIGHT) {
            dragging = true;
            lastDeltaX = 0;
            lastDeltaY = 0;
            return true;
        }

        if (isMouseOverElement(mouseX, mouseY)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        return false;
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

    public record PowerSettings(int maxParallel, long voltagePerParallel) {}
}
