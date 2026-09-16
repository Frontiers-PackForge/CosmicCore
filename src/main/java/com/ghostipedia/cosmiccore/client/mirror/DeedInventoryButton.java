package com.ghostipedia.cosmiccore.client.mirror;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public final class DeedInventoryButton extends Button {

    private static final int BUTTON_GAP = 2;
    private static final int BUTTON_HEIGHT = 18;
    private static final int BUTTON_HORIZONTAL_PADDING = 12;
    private static final int TEXTURE_WIDTH = 28;
    private static final int TEXTURE_HEIGHT = 28;
    private static final int FRAME_INSET = 4;
    private static final long HOLD_DURATION_MS = 500L;
    private static final ResourceLocation BUTTON_TEXTURE = CosmicCore
            .id("textures/gui/mirror/deed_inventory_button.png");

    private final InventoryScreen screen;
    private final AbstractWidget recipeButton;
    private boolean holding;
    private boolean keyboardHolding;
    private long holdStartedAt;

    DeedInventoryButton(InventoryScreen screen, AbstractWidget recipeButton, Component message, OnPress onPress) {
        super(
                buttonX(screen, recipeButton, buttonWidth(message)),
                recipeButton.getY(),
                buttonWidth(message),
                BUTTON_HEIGHT,
                message,
                onPress,
                DEFAULT_NARRATION);
        this.screen = screen;
        this.recipeButton = recipeButton;
    }

    public static boolean visibleOnScreen(InventoryScreen screen) {
        return screen.width >= 379 || !screen.getRecipeBookComponent().isVisible();
    }

    @Override
    public boolean isActive() {
        return visibleOnScreen(screen) && super.isActive();
    }

    @Override
    public boolean isHovered() {
        return visibleOnScreen(screen) && super.isHovered();
    }

    @Override
    public NarrationPriority narrationPriority() {
        return visibleOnScreen(screen) ? super.narrationPriority() : NarrationPriority.NONE;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isActive() || !visible || !CommonInputs.selected(keyCode)) return false;
        if (!holding) playDownSound(Minecraft.getInstance().getSoundManager());
        startHolding(true);
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (!keyboardHolding || !CommonInputs.selected(keyCode)) return false;
        resetHold();
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isActive() || !visible || button != GLFW.GLFW_MOUSE_BUTTON_LEFT || !isMouseOver(mouseX, mouseY))
            return false;
        playDownSound(Minecraft.getInstance().getSoundManager());
        startHolding(false);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!holding || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        resetHold();
        return true;
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent event) {
        return visibleOnScreen(screen) ? super.nextFocusPath(event) : null;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        setPosition(buttonX(screen, recipeButton, getWidth()), recipeButton.getY());
        if (!visibleOnScreen(screen)) {
            setFocused(false);
            resetHold();
            return;
        }

        float holdProgress = updateHold(mouseX, mouseY);

        float brightness = isHoveredOrFocused() || holding ? 1.0F : 0.86F;
        guiGraphics.setColor(brightness, brightness, brightness, alpha);
        drawFrame(guiGraphics);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (holdProgress > 0.0F) {
            int fillWidth = Mth.ceil((getWidth() - FRAME_INSET * 2) * holdProgress);
            guiGraphics.fill(getX() + FRAME_INSET, getY() + FRAME_INSET,
                    getX() + FRAME_INSET + fillWidth, getY() + getHeight() - FRAME_INSET,
                    0x708A5C10);
        }

        int textColor = isHoveredOrFocused() || holding ? 0xFFFFE8A3 : 0xFFE4C16D;
        guiGraphics.drawCenteredString(
                Minecraft.getInstance().font,
                getMessage(),
                getX() + getWidth() / 2,
                getY() + (getHeight() - 8) / 2,
                textColor);
    }

    private void drawFrame(GuiGraphics guiGraphics) {
        guiGraphics.blit(BUTTON_TEXTURE, getX(), getY(), getWidth(), getHeight(), 0, 0, TEXTURE_WIDTH,
                TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private static int buttonX(InventoryScreen screen, AbstractWidget recipeButton, int width) {
        int preferredX = recipeButton.getX() + recipeButton.getWidth() + BUTTON_GAP;
        int maximumX = screen.getGuiLeft() + screen.getXSize() - width - BUTTON_GAP;
        if (preferredX <= maximumX) return preferredX;
        return Math.max(screen.getGuiLeft() + BUTTON_GAP, recipeButton.getX() - width - BUTTON_GAP);
    }

    private static int buttonWidth(Component message) {
        return Math.max(BUTTON_HEIGHT, Minecraft.getInstance().font.width(message) + BUTTON_HORIZONTAL_PADDING);
    }

    private void startHolding(boolean keyboard) {
        if (holding) return;
        holding = true;
        keyboardHolding = keyboard;
        holdStartedAt = Util.getMillis();
    }

    private float updateHold(int mouseX, int mouseY) {
        if (!holding) return 0.0F;
        Minecraft minecraft = Minecraft.getInstance();
        boolean inputHeld = keyboardHolding || GLFW.glfwGetMouseButton(
                minecraft.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        if (!isActive() || !minecraft.isWindowActive() || !inputHeld || (keyboardHolding && !isFocused()) ||
                (!keyboardHolding && !isMouseOver(mouseX, mouseY))) {
            resetHold();
            return 0.0F;
        }

        float progress = Mth.clamp((float) (Util.getMillis() - holdStartedAt) / HOLD_DURATION_MS, 0.0F, 1.0F);
        if (progress >= 1.0F) {
            resetHold();
            onPress();
        }
        return progress;
    }

    private void resetHold() {
        holding = false;
        keyboardHolding = false;
        holdStartedAt = 0L;
    }
}
