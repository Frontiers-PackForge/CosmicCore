package com.ghostipedia.cosmiccore.client.gui;

import com.ghostipedia.cosmiccore.common.data.CosmicSounds;
import com.ghostipedia.cosmiccore.common.item.behavior.ExtendedDyeColor;
import com.ghostipedia.cosmiccore.common.item.behavior.InfiniteSprayCanBehavior;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class SprayCanScreen extends Screen {

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 90;
    private static final int BUTTON_SIZE = 18;
    private static final int GRID_COLS = 8;
    private static final int GRID_ROWS = 2;
    private static final int PADDING = 8;

    private static final int BG_TOP = 0xFF080c14;
    private static final int BG_BOTTOM = 0xFF040608;
    private static final int GRID_COLOR = 0x0A4080FF;
    private static final int BORDER_COLOR = 0xFF304060;
    private static final int ACCENT_COLOR = 0xFF4080C0;
    private static final int BUTTON_BG = 0xC0101018;
    private static final int BUTTON_BORDER = 0xFF405070;
    private static final int BUTTON_HOVER = 0xFF6090D0;

    private final Player player;
    private final InfiniteSprayCanBehavior behavior;

    private int guiLeft;
    private int guiTop;
    private int hoveredColorIndex = -1;

    public SprayCanScreen(Player player, InfiniteSprayCanBehavior behavior) {
        super(Component.translatable("cosmiccore.item.spraycan.gui.title"));
        this.player = player;
        this.behavior = behavior;
    }

    @Override
    protected void init() {
        super.init();
        guiLeft = (width - GUI_WIDTH) / 2;
        guiTop = (height - GUI_HEIGHT) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        drawBackground(graphics);
        drawColorGrid(graphics, mouseX, mouseY);
        drawSolventButton(graphics, mouseX, mouseY);
        drawCurrentColor(graphics);
        drawTooltip(graphics, mouseX, mouseY);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawBackground(GuiGraphics graphics) {
        // Gradient background
        for (int row = 0; row < GUI_HEIGHT; row++) {
            float progress = (float) row / GUI_HEIGHT;
            int color = lerpColor(BG_TOP, BG_BOTTOM, progress);
            graphics.fill(guiLeft, guiTop + row, guiLeft + GUI_WIDTH, guiTop + row + 1, color);
        }

        // Grid pattern
        int spacing = 10;
        for (int gx = guiLeft + spacing; gx < guiLeft + GUI_WIDTH; gx += spacing) {
            graphics.fill(gx, guiTop, gx + 1, guiTop + GUI_HEIGHT, GRID_COLOR);
        }
        for (int gy = guiTop + spacing; gy < guiTop + GUI_HEIGHT; gy += spacing) {
            graphics.fill(guiLeft, gy, guiLeft + GUI_WIDTH, gy + 1, GRID_COLOR);
        }

        // Border
        drawBorder(graphics, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT, BORDER_COLOR);

        // Corner accents
        drawCornerAccents(graphics, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT, ACCENT_COLOR);

        // Title
        var font = Minecraft.getInstance().font;
        Component title = getTitle();
        int titleWidth = font.width(title);
        int titleX = guiLeft + (GUI_WIDTH - titleWidth) / 2;
        graphics.drawString(font, title, titleX, guiTop + 4, ACCENT_COLOR, true);
    }

    private void drawColorGrid(GuiGraphics graphics, int mouseX, int mouseY) {
        hoveredColorIndex = -1;
        int startX = guiLeft + PADDING;
        int startY = guiTop + 18;

        for (int i = 0; i < 16; i++) {
            ExtendedDyeColor dyeColor = ExtendedDyeColor.values()[i];
            int col = i % GRID_COLS;
            int row = i / GRID_COLS;
            int x = startX + col * BUTTON_SIZE + col * 2;
            int y = startY + row * BUTTON_SIZE + row * 2;

            boolean hovered = mouseX >= x && mouseX < x + BUTTON_SIZE && mouseY >= y && mouseY < y + BUTTON_SIZE;
            boolean selected = behavior.getColor() == dyeColor;

            if (hovered) {
                hoveredColorIndex = i;
            }

            drawColorButton(graphics, x, y, dyeColor, hovered, selected);
        }
    }

    private void drawColorButton(GuiGraphics graphics, int x, int y, ExtendedDyeColor color, boolean hovered,
                                 boolean selected) {
        // Button background
        graphics.fill(x, y, x + BUTTON_SIZE, y + BUTTON_SIZE, BUTTON_BG);

        // Border
        int borderColor = selected ? ACCENT_COLOR : (hovered ? BUTTON_HOVER : BUTTON_BORDER);
        drawBorder(graphics, x, y, BUTTON_SIZE, BUTTON_SIZE, borderColor);

        // Color fill (inner area)
        int innerPad = 2;
        int dyeRgb = color.getTextColor();
        int fillColor = 0xFF000000 | dyeRgb;
        graphics.fill(x + innerPad, y + innerPad, x + BUTTON_SIZE - innerPad, y + BUTTON_SIZE - innerPad, fillColor);
    }

    private void drawSolventButton(GuiGraphics graphics, int mouseX, int mouseY) {
        // Align to rightmost column of the grid
        int gridWidth = GRID_COLS * BUTTON_SIZE + (GRID_COLS - 1) * 2;
        int x = guiLeft + PADDING + gridWidth - BUTTON_SIZE;
        int y = guiTop + GUI_HEIGHT - 28;

        boolean hovered = mouseX >= x && mouseX < x + BUTTON_SIZE && mouseY >= y && mouseY < y + BUTTON_SIZE;
        boolean selected = behavior.getColor() == ExtendedDyeColor.SOLVENT;

        if (hovered) {
            hoveredColorIndex = 16; // Solvent index
        }

        // Button background
        graphics.fill(x, y, x + BUTTON_SIZE, y + BUTTON_SIZE, BUTTON_BG);

        // Border
        int borderColor = selected ? ACCENT_COLOR : (hovered ? BUTTON_HOVER : BUTTON_BORDER);
        drawBorder(graphics, x, y, BUTTON_SIZE, BUTTON_SIZE, borderColor);

        // X pattern for solvent
        int innerPad = 4;
        int lineColor = 0xFFCC4444;
        // Draw X
        for (int i = 0; i < BUTTON_SIZE - innerPad * 2; i++) {
            graphics.fill(x + innerPad + i, y + innerPad + i, x + innerPad + i + 1, y + innerPad + i + 2, lineColor);
            graphics.fill(x + BUTTON_SIZE - innerPad - i - 1, y + innerPad + i, x + BUTTON_SIZE - innerPad - i,
                    y + innerPad + i + 2, lineColor);
        }
    }

    private void drawCurrentColor(GuiGraphics graphics) {
        var font = Minecraft.getInstance().font;
        int y = guiTop + 18 + GRID_ROWS * (BUTTON_SIZE + 2) + 4;

        ExtendedDyeColor current = behavior.getColor();
        String colorName = current != null ? current.name().replace('_', ' ') : "NONE";
        int textColor = getReadableTextColor(current);

        String label = "Color: ";
        graphics.drawString(font, label, guiLeft + PADDING, y, 0xFFAAAAAA, false);
        graphics.drawString(font, colorName, guiLeft + PADDING + font.width(label), y, textColor, false);
    }

    private int getReadableTextColor(ExtendedDyeColor color) {
        if (color == null || color == ExtendedDyeColor.SOLVENT) {
            return 0xFFFFFFFF;
        }
        // Black and other dark colors need a lighter display color
        if (color == ExtendedDyeColor.BLACK) {
            return 0xFF666666;
        }
        return 0xFF000000 | color.getTextColor();
    }

    private void drawTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (hoveredColorIndex < 0) return;

        Component tooltip;
        if (hoveredColorIndex == 16) {
            tooltip = Component.translatable("cosmiccore.item.spraycan.gui.solvent");
        } else {
            String colorName = ExtendedDyeColor.values()[hoveredColorIndex].name().replace('_', ' ');
            tooltip = Component.literal(colorName);
        }

        graphics.renderTooltip(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int clickedIndex = getColorIndexAt((int) mouseX, (int) mouseY);
            if (clickedIndex >= 0) {
                ExtendedDyeColor newColor;
                if (clickedIndex == 16) {
                    newColor = ExtendedDyeColor.SOLVENT;
                } else {
                    newColor = ExtendedDyeColor.values()[clickedIndex];
                }
                behavior.setColor(newColor);
                behavior.sendColorToTag(player, newColor);
                Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(CosmicSounds.SHAKE_CAN.getMainEvent(), 1.0f, 1.0f));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private int getColorIndexAt(int mouseX, int mouseY) {
        int startX = guiLeft + PADDING;
        int startY = guiTop + 18;

        // Check color grid
        for (int i = 0; i < 16; i++) {
            int col = i % GRID_COLS;
            int row = i / GRID_COLS;
            int x = startX + col * BUTTON_SIZE + col * 2;
            int y = startY + row * BUTTON_SIZE + row * 2;

            if (mouseX >= x && mouseX < x + BUTTON_SIZE && mouseY >= y && mouseY < y + BUTTON_SIZE) {
                return i;
            }
        }

        // Check solvent button (aligned to rightmost column)
        int gridWidth = GRID_COLS * BUTTON_SIZE + (GRID_COLS - 1) * 2;
        int solventX = guiLeft + PADDING + gridWidth - BUTTON_SIZE;
        int solventY = guiTop + GUI_HEIGHT - 28;
        if (mouseX >= solventX && mouseX < solventX + BUTTON_SIZE && mouseY >= solventY &&
                mouseY < solventY + BUTTON_SIZE) {
            return 16;
        }

        return -1;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void drawBorder(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + 1, color); // Top
        graphics.fill(x, y + h - 1, x + w, y + h, color); // Bottom
        graphics.fill(x, y, x + 1, y + h, color); // Left
        graphics.fill(x + w - 1, y, x + w, y + h, color); // Right
    }

    private void drawCornerAccents(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        int len = 12;
        int thickness = 2;
        int accentAlpha = (color & 0x00FFFFFF) | 0x60000000;

        // Top-left
        graphics.fill(x, y, x + len, y + thickness, accentAlpha);
        graphics.fill(x, y, x + thickness, y + len, accentAlpha);
        // Top-right
        graphics.fill(x + w - len, y, x + w, y + thickness, accentAlpha);
        graphics.fill(x + w - thickness, y, x + w, y + len, accentAlpha);
        // Bottom-left
        graphics.fill(x, y + h - thickness, x + len, y + h, accentAlpha);
        graphics.fill(x, y + h - len, x + thickness, y + h, accentAlpha);
        // Bottom-right
        graphics.fill(x + w - len, y + h - thickness, x + w, y + h, accentAlpha);
        graphics.fill(x + w - thickness, y + h - len, x + w, y + h, accentAlpha);
    }

    private int lerpColor(int c1, int c2, float t) {
        int a1 = (c1 >> 24) & 0xFF, a2 = (c2 >> 24) & 0xFF;
        int r1 = (c1 >> 16) & 0xFF, r2 = (c2 >> 16) & 0xFF;
        int g1 = (c1 >> 8) & 0xFF, g2 = (c2 >> 8) & 0xFF;
        int b1 = c1 & 0xFF, b2 = c2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
