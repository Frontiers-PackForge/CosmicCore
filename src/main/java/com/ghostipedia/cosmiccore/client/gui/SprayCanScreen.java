package com.ghostipedia.cosmiccore.client.gui;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.item.behavior.ExtendedDyeColor;
import com.ghostipedia.cosmiccore.common.item.behavior.SprayCanClientHandler;
import com.ghostipedia.cosmiccore.common.item.behavior.SprayCanState;
import com.ghostipedia.cosmiccore.common.network.packet.SprayCanStatePacket;

import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SprayCanScreen extends Screen {

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 112;
    private static final int BUTTON_SIZE = 18;
    private static final int GRID_COLS = 8;
    private static final int GRID_ROWS = 2;
    private static final int PADDING = 8;
    private static final int MODE_BUTTON_WIDTH = 50;
    private static final int MODE_BUTTON_HEIGHT = 14;
    private static final int MODE_BUTTON_GAP = 3;

    private static final ResourceLocation PANEL = CosmicCore.id("textures/gui/rate_calculator/panel.png");
    private static final ResourceLocation BUTTON = CosmicCore.id("textures/gui/rate_calculator/button.png");
    private static final ResourceLocation BUTTON_HIGHLIGHTED = CosmicCore
            .id("textures/gui/rate_calculator/button_highlighted.png");

    private final Player player;
    private final InteractionHand hand;

    private int guiLeft;
    private int guiTop;
    private int hoveredColorIndex = -1;
    private int hoveredModeIndex = -1;

    public SprayCanScreen(Player player, InteractionHand hand) {
        super(Component.translatable("cosmiccore.item.spraycan.gui.title"));
        this.player = player;
        this.hand = hand;
    }

    @Override
    protected void init() {
        super.init();
        guiLeft = (width - GUI_WIDTH) / 2;
        guiTop = (height - GUI_HEIGHT) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        drawBackground(graphics);
        drawColorGrid(graphics, mouseX, mouseY);
        drawSpecialButtons(graphics, mouseX, mouseY);
        drawCurrentColor(graphics);
        drawModeButtons(graphics, mouseX, mouseY);
        drawTooltip(graphics, mouseX, mouseY);
    }

    private void drawBackground(GuiGraphics graphics) {
        drawNineSlice(graphics, PANEL, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT, 9, 62, 62);
        var font = Minecraft.getInstance().font;
        Component title = getTitle();
        int titleWidth = font.width(title);
        int titleX = guiLeft + (GUI_WIDTH - titleWidth) / 2;
        graphics.drawString(font, title, titleX, guiTop + 7, 0xFFFFFFFF, true);
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
            SprayCanState state = SprayCanState.read(stack());
            boolean selected = !state.rainSealant() && state.color() == dyeColor;

            if (hovered) {
                hoveredColorIndex = i;
            }

            drawColorButton(graphics, x, y, dyeColor, hovered, selected);
        }
    }

    private void drawColorButton(GuiGraphics graphics, int x, int y, ExtendedDyeColor color, boolean hovered,
                                 boolean selected) {
        drawButton(graphics, x, y, BUTTON_SIZE, BUTTON_SIZE, hovered || selected);
        graphics.renderItem(dyeStack(color), x + 1, y + 1);
    }

    private void drawSpecialButtons(GuiGraphics graphics, int mouseX, int mouseY) {
        int gridWidth = GRID_COLS * BUTTON_SIZE + (GRID_COLS - 1) * 2;
        int x = guiLeft + PADDING + gridWidth - BUTTON_SIZE;
        int y = guiTop + 62;

        int rainX = x - BUTTON_SIZE - 2;
        boolean rainHovered = mouseX >= rainX && mouseX < rainX + BUTTON_SIZE && mouseY >= y &&
                mouseY < y + BUTTON_SIZE;
        if (rainHovered) hoveredColorIndex = 16;
        drawButton(graphics, rainX, y, BUTTON_SIZE, BUTTON_SIZE,
                rainHovered || SprayCanState.read(stack()).rainSealant());
        graphics.renderItem(CosmicItems.RAIN_SEALANT_SPRAY_CAN.asStack(), rainX + 1, y + 1);

        boolean hovered = mouseX >= x && mouseX < x + BUTTON_SIZE && mouseY >= y && mouseY < y + BUTTON_SIZE;
        boolean selected = !SprayCanState.read(stack()).rainSealant() &&
                SprayCanState.read(stack()).color() == ExtendedDyeColor.SOLVENT;

        if (hovered) {
            hoveredColorIndex = 17;
        }
        drawButton(graphics, x, y, BUTTON_SIZE, BUTTON_SIZE, hovered || selected);
        graphics.renderItem(GTItems.SPRAY_SOLVENT.asStack(), x + 1, y + 1);
    }

    private void drawCurrentColor(GuiGraphics graphics) {
        var font = Minecraft.getInstance().font;
        int y = guiTop + 18 + GRID_ROWS * (BUTTON_SIZE + 2) + 4;

        SprayCanState state = SprayCanState.read(stack());
        ExtendedDyeColor current = state.color();
        Component colorName = state.rainSealant() ?
                Component.translatable("material.cosmiccore.rain_sealant") :
                current.isSolvent() ? Component.translatable("cosmiccore.item.spraycan.gui.solvent") :
                        dyeName(current);
        int textColor = getReadableTextColor(current);

        Component label = Component.translatable("cosmiccore.item.spraycan.gui.color");
        graphics.drawString(font, label, guiLeft + PADDING, y, 0xFFAAAAAA, false);
        int valueX = guiLeft + PADDING + font.width(label);
        int availableWidth = guiLeft + PADDING + 6 * (BUTTON_SIZE + 2) - 2 - valueX;
        graphics.drawString(font, font.plainSubstrByWidth(colorName.getString(), availableWidth), valueX, y,
                state.rainSealant() ? 0xFF9CD7FF : textColor, false);
    }

    private void drawModeButtons(GuiGraphics graphics, int mouseX, int mouseY) {
        hoveredModeIndex = -1;
        var font = Minecraft.getInstance().font;
        SprayCanState.SprayMode selectedMode = SprayCanState.read(stack()).mode();
        int startX = guiLeft + PADDING;
        int y = guiTop + GUI_HEIGHT - MODE_BUTTON_HEIGHT - PADDING;
        for (int i = 0; i < SprayCanState.SprayMode.values().length; i++) {
            int x = startX + i * (MODE_BUTTON_WIDTH + MODE_BUTTON_GAP);
            boolean hovered = mouseX >= x && mouseX < x + MODE_BUTTON_WIDTH && mouseY >= y &&
                    mouseY < y + MODE_BUTTON_HEIGHT;
            if (hovered) hoveredModeIndex = i;
            boolean selected = selectedMode.ordinal() == i;
            drawButton(graphics, x, y, MODE_BUTTON_WIDTH, MODE_BUTTON_HEIGHT, selected || hovered);
            Component name = Component.translatable("cosmiccore.item.spraycan.mode." +
                    SprayCanState.SprayMode.values()[i].name().toLowerCase());
            if (SprayCanState.SprayMode.values()[i] == SprayCanState.SprayMode.CONNECTED_COLOR) {
                float scale = Math.min(1.0f, (MODE_BUTTON_WIDTH - 4.0f) / font.width(name));
                graphics.pose().pushPose();
                graphics.pose().translate(x + MODE_BUTTON_WIDTH / 2.0f, y + 3.0f, 0.0f);
                graphics.pose().scale(scale, scale, 1.0f);
                graphics.drawString(font, name, -font.width(name) / 2, 0, 0xFFFFFFFF, false);
                graphics.pose().popPose();
            } else {
                graphics.drawCenteredString(font, name, x + MODE_BUTTON_WIDTH / 2, y + 3, 0xFFFFFFFF);
            }
        }
    }

    private int getReadableTextColor(ExtendedDyeColor color) {
        if (color == null || color == ExtendedDyeColor.SOLVENT) {
            return 0xFFFFFFFF;
        }
        if (color == ExtendedDyeColor.BLACK) {
            return 0xFF666666;
        }
        return 0xFF000000 | color.getTextColor();
    }

    private void drawTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (hoveredColorIndex < 0 && hoveredModeIndex < 0) return;

        Component tooltip;
        if (hoveredModeIndex >= 0) {
            tooltip = Component.translatable("cosmiccore.item.spraycan.mode.tooltip." +
                    SprayCanState.SprayMode.values()[hoveredModeIndex].name().toLowerCase());
        } else if (hoveredColorIndex == 16) {
            tooltip = Component.translatable("cosmiccore.item.spraycan.gui.rain_sealant");
        } else if (hoveredColorIndex == 17) {
            tooltip = Component.translatable("cosmiccore.item.spraycan.gui.solvent");
        } else {
            tooltip = dyeName(ExtendedDyeColor.values()[hoveredColorIndex]);
        }

        graphics.renderTooltip(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int clickedIndex = getColorIndexAt((int) mouseX, (int) mouseY);
            if (clickedIndex >= 0) {
                if (clickedIndex == 16) {
                    SprayCanClientHandler.updateState(hand, stack(), SprayCanStatePacket.Action.SET_RAIN_SEALANT, 1);
                } else {
                    ExtendedDyeColor newColor = clickedIndex == 17 ? ExtendedDyeColor.SOLVENT :
                            ExtendedDyeColor.values()[clickedIndex];
                    SprayCanClientHandler.updateState(hand, stack(), SprayCanStatePacket.Action.SET_COLOR,
                            newColor.ordinal());
                }
                return true;
            }
            int modeIndex = getModeIndexAt((int) mouseX, (int) mouseY);
            if (modeIndex >= 0) {
                SprayCanClientHandler.updateState(hand, stack(), SprayCanStatePacket.Action.SET_MODE, modeIndex);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private int getColorIndexAt(int mouseX, int mouseY) {
        int startX = guiLeft + PADDING;
        int startY = guiTop + 18;

        for (int i = 0; i < 16; i++) {
            int col = i % GRID_COLS;
            int row = i / GRID_COLS;
            int x = startX + col * BUTTON_SIZE + col * 2;
            int y = startY + row * BUTTON_SIZE + row * 2;

            if (mouseX >= x && mouseX < x + BUTTON_SIZE && mouseY >= y && mouseY < y + BUTTON_SIZE) {
                return i;
            }
        }

        int gridWidth = GRID_COLS * BUTTON_SIZE + (GRID_COLS - 1) * 2;
        int solventX = guiLeft + PADDING + gridWidth - BUTTON_SIZE;
        int solventY = guiTop + 62;
        int rainX = solventX - BUTTON_SIZE - 2;
        if (mouseX >= rainX && mouseX < rainX + BUTTON_SIZE && mouseY >= solventY && mouseY < solventY + BUTTON_SIZE) {
            return 16;
        }
        if (mouseX >= solventX && mouseX < solventX + BUTTON_SIZE && mouseY >= solventY &&
                mouseY < solventY + BUTTON_SIZE) {
            return 17;
        }

        return -1;
    }

    private int getModeIndexAt(int mouseX, int mouseY) {
        int startX = guiLeft + PADDING;
        int y = guiTop + GUI_HEIGHT - MODE_BUTTON_HEIGHT - PADDING;
        for (int i = 0; i < SprayCanState.SprayMode.values().length; i++) {
            int x = startX + i * (MODE_BUTTON_WIDTH + MODE_BUTTON_GAP);
            if (mouseX >= x && mouseX < x + MODE_BUTTON_WIDTH && mouseY >= y &&
                    mouseY < y + MODE_BUTTON_HEIGHT)
                return i;
        }
        return -1;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private ItemStack stack() {
        return player.getItemInHand(hand);
    }

    private static ItemStack dyeStack(ExtendedDyeColor color) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(
                color.getColor().getName() + "_dye")));
    }

    private static Component dyeName(ExtendedDyeColor color) {
        return Component.translatable("item.minecraft." + color.getColor().getName() + "_dye");
    }

    private static void drawButton(GuiGraphics graphics, int x, int y, int width, int height, boolean highlighted) {
        drawNineSlice(graphics, highlighted ? BUTTON_HIGHLIGHTED : BUTTON, x, y, width, height, 3, 200, 20);
    }

    private static void drawNineSlice(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width,
                                      int height, int border, int textureWidth, int textureHeight) {
        int centerWidth = Math.max(0, width - border * 2);
        int centerHeight = Math.max(0, height - border * 2);
        int sourceCenterWidth = textureWidth - border * 2;
        int sourceCenterHeight = textureHeight - border * 2;
        blit(graphics, texture, x, y, border, border, 0, 0, border, border, textureWidth, textureHeight);
        blit(graphics, texture, x + border, y, centerWidth, border, border, 0, sourceCenterWidth, border,
                textureWidth, textureHeight);
        blit(graphics, texture, x + border + centerWidth, y, border, border, textureWidth - border, 0, border,
                border, textureWidth, textureHeight);
        blit(graphics, texture, x, y + border, border, centerHeight, 0, border, border, sourceCenterHeight,
                textureWidth, textureHeight);
        blit(graphics, texture, x + border, y + border, centerWidth, centerHeight, border, border,
                sourceCenterWidth, sourceCenterHeight, textureWidth, textureHeight);
        blit(graphics, texture, x + border + centerWidth, y + border, border, centerHeight, textureWidth - border,
                border, border, sourceCenterHeight, textureWidth, textureHeight);
        blit(graphics, texture, x, y + border + centerHeight, border, border, 0, textureHeight - border, border,
                border, textureWidth, textureHeight);
        blit(graphics, texture, x + border, y + border + centerHeight, centerWidth, border, border,
                textureHeight - border, sourceCenterWidth, border, textureWidth, textureHeight);
        blit(graphics, texture, x + border + centerWidth, y + border + centerHeight, border, border,
                textureWidth - border, textureHeight - border, border, border, textureWidth, textureHeight);
    }

    private static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height,
                             int sourceX, int sourceY, int sourceWidth, int sourceHeight, int textureWidth,
                             int textureHeight) {
        if (width > 0 && height > 0) graphics.blit(texture, x, y, width, height, sourceX, sourceY, sourceWidth,
                sourceHeight, textureWidth, textureHeight);
    }
}
