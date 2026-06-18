package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.lowdragmc.lowdraglib.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.IntConsumer;

import javax.annotation.Nonnull;

public class StarColorPickerPopup extends WidgetGroup {

    public static final int WIDTH = 160;
    public static final int HEIGHT = 140;
    private static final int TITLE_HEIGHT = 16;
    private static final int PICKER_SIZE = 80;
    private static final int HUE_BAR_WIDTH = 12;

    private final Runnable onClose;
    private final IntConsumer onColorChanged;

    // Current color in HSB
    private float hue = 0.15f;       // Default yellow-ish
    private float saturation = 0.8f;
    private float brightness = 1.0f;

    // Current color as RGB
    private int currentColor = 0xFFCC44;

    // Text field for hex input
    private TextFieldWidget hexField;

    // Dragging state
    private boolean draggingPicker = false;
    private boolean draggingHue = false;
    private boolean draggingTitle = false;
    private double lastDeltaX, lastDeltaY;

    // Animation
    private float appearProgress = 0f;

    public StarColorPickerPopup(int x, int y, Runnable onClose, IntConsumer onColorChanged) {
        super(x, y, WIDTH, HEIGHT);
        this.onClose = onClose;
        this.onColorChanged = onColorChanged;
        setVisible(false);
        initWidgets();
    }

    private void initWidgets() {
        // Hex input field at bottom
        int fieldX = 6;
        int fieldY = HEIGHT - 26;
        int fieldWidth = WIDTH - 60;

        hexField = new TextFieldWidget(fieldX, fieldY, fieldWidth, 16,
                this::getHexString,
                this::onHexChanged);
        hexField.setClientSideWidget();
        hexField.setMaxStringLength(7); // #RRGGBB
        hexField.setBackground(new com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup(
                new ColorRectTexture(0xE0101018),
                new ColorBorderTexture(1, 0xFF404060)));
        addWidget(hexField);
    }

    public void show(int color) {
        if (color == -1) {
            // Default - use a nice yellow
            currentColor = 0xFFCC44;
        } else {
            currentColor = color & 0xFFFFFF;
        }

        // Convert to HSB
        float[] hsb = rgbToHsb(currentColor);
        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];

        appearProgress = 0f;
        setVisible(true);
        setActive(true);

        updateHexField();
    }

    public void hide() {
        setVisible(false);
        setActive(false);
    }

    private String getHexString() {
        return String.format("#%06X", currentColor & 0xFFFFFF);
    }

    private void onHexChanged(String text) {
        try {
            String hex = text.startsWith("#") ? text.substring(1) : text;
            if (hex.length() == 6) {
                int color = Integer.parseInt(hex, 16);
                setColor(color);
            }
        } catch (NumberFormatException ignored) {}
    }

    private void setColor(int rgb) {
        currentColor = rgb & 0xFFFFFF;
        float[] hsb = rgbToHsb(currentColor);
        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];

        if (onColorChanged != null) {
            onColorChanged.accept(currentColor);
        }
    }

    private void updateFromHsb() {
        currentColor = hsbToRgb(hue, saturation, brightness);
        updateHexField();

        if (onColorChanged != null) {
            onColorChanged.accept(currentColor);
        }
    }

    private void updateHexField() {
        if (hexField != null) {
            hexField.setCurrentString(getHexString());
        }
    }

    private void resetToDefault() {
        if (onColorChanged != null) {
            onColorChanged.accept(-1); // -1 signals default
        }
        hide();
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

        // Background
        int bgAlpha = (int) (0xE8 * alpha);
        int bgColor = (bgAlpha << 24) | 0x0c0c14;
        DrawerHelper.drawSolidRect(graphics, x, y, w, h, bgColor);

        // Grid pattern
        int gridAlpha = (int) (0x08 * alpha);
        int gridColor = (gridAlpha << 24) | 0xFFFFFF;
        for (int gx = x + 8; gx < x + w; gx += 8) {
            graphics.fill(gx, y, gx + 1, y + h, gridColor);
        }
        for (int gy = y + 8; gy < y + h; gy += 8) {
            graphics.fill(x, gy, x + w, gy + 1, gridColor);
        }

        // Title bar
        int titleBgAlpha = (int) (0xD0 * alpha);
        int titleBgColor = (titleBgAlpha << 24) | 0x101820;
        DrawerHelper.drawSolidRect(graphics, x, y, w, TITLE_HEIGHT, titleBgColor);

        // Border
        int borderAlpha = (int) (0x80 * alpha);
        int borderColor = (borderAlpha << 24) | 0x4080FF;
        DrawerHelper.drawBorder(graphics, x, y, w, h, borderColor, 1);

        // Title bar accent
        graphics.fill(x + 1, y + TITLE_HEIGHT - 2, x + w - 1, y + TITLE_HEIGHT, borderColor);

        // Draw title
        drawTitle(graphics, x, y, w, alpha);

        // Draw color picker
        drawColorPicker(graphics, x, y, mouseX, mouseY, alpha);

        // Draw close button
        drawCloseButton(graphics, x + w - 14, y + 3, mouseX, mouseY, alpha);

        // Draw reset button
        drawResetButton(graphics, x + w - 50, y + HEIGHT - 26, mouseX, mouseY, alpha);

        // Draw color preview
        drawColorPreview(graphics, x, y, alpha);

        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
    }

    private void drawTitle(GuiGraphics graphics, int x, int y, int w, float alpha) {
        var font = Minecraft.getInstance().font;
        String title = "Star Color";
        int textColor = (int) (0xFF * alpha) << 24 | 0xFFFFFF;
        graphics.drawString(font, title, x + 4, y + (TITLE_HEIGHT - font.lineHeight) / 2 + 1, textColor, false);
    }

    private void drawColorPicker(GuiGraphics graphics, int baseX, int baseY, int mouseX, int mouseY, float alpha) {
        int pickerX = baseX + 6;
        int pickerY = baseY + TITLE_HEIGHT + 4;

        // Draw saturation/brightness gradient
        for (int py = 0; py < PICKER_SIZE; py++) {
            for (int px = 0; px < PICKER_SIZE; px++) {
                float s = (float) px / PICKER_SIZE;
                float b = 1f - (float) py / PICKER_SIZE;
                int color = hsbToRgb(hue, s, b);
                int pixelAlpha = (int) (0xFF * alpha);
                graphics.fill(pickerX + px, pickerY + py, pickerX + px + 1, pickerY + py + 1,
                        (pixelAlpha << 24) | color);
            }
        }

        // Border around picker
        int pickerBorder = (int) (0x80 * alpha) << 24 | 0x606080;
        DrawerHelper.drawBorder(graphics, pickerX, pickerY, PICKER_SIZE, PICKER_SIZE, pickerBorder, 1);

        // Draw crosshair at current position
        int crossX = pickerX + (int) (saturation * PICKER_SIZE);
        int crossY = pickerY + (int) ((1f - brightness) * PICKER_SIZE);
        int crossColor = (int) (0xFF * alpha) << 24 | 0xFFFFFF;
        graphics.fill(crossX - 4, crossY, crossX + 5, crossY + 1, crossColor);
        graphics.fill(crossX, crossY - 4, crossX + 1, crossY + 5, crossColor);

        // Draw hue bar
        int hueX = pickerX + PICKER_SIZE + 6;
        for (int py = 0; py < PICKER_SIZE; py++) {
            float h = (float) py / PICKER_SIZE;
            int color = hsbToRgb(h, 1f, 1f);
            int pixelAlpha = (int) (0xFF * alpha);
            graphics.fill(hueX, pickerY + py, hueX + HUE_BAR_WIDTH, pickerY + py + 1, (pixelAlpha << 24) | color);
        }

        // Border around hue bar
        DrawerHelper.drawBorder(graphics, hueX, pickerY, HUE_BAR_WIDTH, PICKER_SIZE, pickerBorder, 1);

        // Draw hue indicator
        int hueY = pickerY + (int) (hue * PICKER_SIZE);
        graphics.fill(hueX - 2, hueY - 1, hueX + HUE_BAR_WIDTH + 2, hueY + 2, crossColor);
    }

    private void drawColorPreview(GuiGraphics graphics, int baseX, int baseY, float alpha) {
        int previewX = baseX + 6 + PICKER_SIZE + 6 + HUE_BAR_WIDTH + 8;
        int previewY = baseY + TITLE_HEIGHT + 4;
        int previewSize = 30;

        // Background checkerboard for transparency reference
        int checkSize = 5;
        for (int cy = 0; cy < previewSize / checkSize; cy++) {
            for (int cx = 0; cx < previewSize / checkSize; cx++) {
                int checkColor = ((cx + cy) % 2 == 0) ? 0xFF404040 : 0xFF808080;
                graphics.fill(previewX + cx * checkSize, previewY + cy * checkSize,
                        previewX + (cx + 1) * checkSize, previewY + (cy + 1) * checkSize, checkColor);
            }
        }

        // Color preview
        int previewAlpha = (int) (0xFF * alpha);
        graphics.fill(previewX, previewY, previewX + previewSize, previewY + previewSize,
                (previewAlpha << 24) | currentColor);

        // Border
        int previewBorder = (int) (0x80 * alpha) << 24 | 0x606080;
        DrawerHelper.drawBorder(graphics, previewX, previewY, previewSize, previewSize, previewBorder, 1);

        // Label
        var font = Minecraft.getInstance().font;
        int textColor = (int) (0xFF * alpha) << 24 | 0xA0A0B0;
        graphics.drawString(font, "Preview", previewX, previewY + previewSize + 4, textColor, false);
    }

    private void drawCloseButton(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, float alpha) {
        int size = 10;
        boolean hovered = mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size;

        int bgColor = hovered ? (int) (0xC0 * alpha) << 24 | 0xFF4444 : (int) (0x60 * alpha) << 24 | 0x404050;
        int fgColor = (int) (0xFF * alpha) << 24 | 0xFFFFFF;

        graphics.fill(x, y, x + size, y + size, bgColor);

        // X mark
        graphics.fill(x + 2, y + 3, x + 4, y + 7, fgColor);
        graphics.fill(x + 6, y + 3, x + 8, y + 7, fgColor);
        graphics.fill(x + 3, y + 4, x + 7, y + 6, fgColor);
    }

    private void drawResetButton(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, float alpha) {
        int bw = 44;
        int bh = 16;
        boolean hovered = mouseX >= x && mouseX < x + bw && mouseY >= y && mouseY < y + bh;

        int bgColor = hovered ? (int) (0xC0 * alpha) << 24 | 0x4080FF : (int) (0x80 * alpha) << 24 | 0x404060;

        graphics.fill(x, y, x + bw, y + bh, bgColor);
        DrawerHelper.drawBorder(graphics, x, y, bw, bh, (int) (0x80 * alpha) << 24 | 0x606080, 1);

        var font = Minecraft.getInstance().font;
        String text = "Reset";
        int textX = x + (bw - font.width(text)) / 2;
        int textY = y + (bh - font.lineHeight) / 2;
        int textColor = (int) (0xFF * alpha) << 24 | 0xFFFFFF;
        graphics.drawString(font, text, textX, textY, textColor, false);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isVisible()) return false;

        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;

        // Check close button
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

        // Check reset button
        int resetX = x + w - 50;
        int resetY = y + HEIGHT - 26;
        if (mouseX >= resetX && mouseX < resetX + 44 && mouseY >= resetY && mouseY < resetY + 16) {
            resetToDefault();
            playButtonClickSound();
            return true;
        }

        // Check color picker area
        int pickerX = x + 6;
        int pickerY = y + TITLE_HEIGHT + 4;
        if (mouseX >= pickerX && mouseX < pickerX + PICKER_SIZE &&
                mouseY >= pickerY && mouseY < pickerY + PICKER_SIZE) {
            draggingPicker = true;
            updatePickerFromMouse(mouseX, mouseY, pickerX, pickerY);
            return true;
        }

        // Check hue bar
        int hueX = pickerX + PICKER_SIZE + 6;
        if (mouseX >= hueX && mouseX < hueX + HUE_BAR_WIDTH &&
                mouseY >= pickerY && mouseY < pickerY + PICKER_SIZE) {
            draggingHue = true;
            updateHueFromMouse(mouseY, pickerY);
            return true;
        }

        // Check title bar for dragging
        if (mouseX >= x && mouseX < x + w - 20 && mouseY >= y && mouseY < y + TITLE_HEIGHT) {
            draggingTitle = true;
            lastDeltaX = 0;
            lastDeltaY = 0;
            return true;
        }

        // Click inside panel
        if (isMouseOverElement(mouseX, mouseY)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        return false;
    }

    private void updatePickerFromMouse(double mouseX, double mouseY, int pickerX, int pickerY) {
        saturation = Mth.clamp((float) (mouseX - pickerX) / PICKER_SIZE, 0f, 1f);
        brightness = Mth.clamp(1f - (float) (mouseY - pickerY) / PICKER_SIZE, 0f, 1f);
        updateFromHsb();
    }

    private void updateHueFromMouse(double mouseY, int pickerY) {
        hue = Mth.clamp((float) (mouseY - pickerY) / PICKER_SIZE, 0f, 1f);
        updateFromHsb();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingPicker) {
            int pickerX = getPosition().x + 6;
            int pickerY = getPosition().y + TITLE_HEIGHT + 4;
            updatePickerFromMouse(mouseX, mouseY, pickerX, pickerY);
            return true;
        }

        if (draggingHue) {
            int pickerY = getPosition().y + TITLE_HEIGHT + 4;
            updateHueFromMouse(mouseY, pickerY);
            return true;
        }

        if (draggingTitle) {
            double dx = dragX + lastDeltaX;
            double dy = dragY + lastDeltaY;
            int intDx = (int) dx;
            int intDy = (int) dy;
            lastDeltaX = dx - intDx;
            lastDeltaY = dy - intDy;
            addSelfPosition(intDx, intDy);
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingPicker || draggingHue || draggingTitle) {
            draggingPicker = false;
            draggingHue = false;
            draggingTitle = false;
            lastDeltaX = 0;
            lastDeltaY = 0;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private static float[] rgbToHsb(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        float[] hsb = new float[3];
        java.awt.Color.RGBtoHSB(r, g, b, hsb);
        return hsb;
    }

    private static int hsbToRgb(float h, float s, float b) {
        return java.awt.Color.HSBtoRGB(h, s, b) & 0xFFFFFF;
    }
}
