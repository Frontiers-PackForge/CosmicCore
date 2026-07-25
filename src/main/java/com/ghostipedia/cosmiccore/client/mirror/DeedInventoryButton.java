package com.ghostipedia.cosmiccore.client.mirror;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.jetbrains.annotations.Nullable;

public final class DeedInventoryButton extends Button {

    public static final int BUTTON_WIDTH = 64;
    public static final int BUTTON_HEIGHT = 20;
    public static final int TEXTURE_WIDTH = 72;
    public static final int TEXTURE_HEIGHT = 28;

    public static final int TEXTURE_BUFFER = 4;
    private static final int CHAIN_TILE_SIZE = 18;
    private static final float CHAIN_SCALE = 0.925F;
    private static final float CHAIN_ANCHOR_OFFSET = 6.0F;
    private static final ResourceLocation BUTTON_TEXTURE = CosmicCore
            .id("textures/gui/mirror/deed_inventory_button.png");
    private static final ResourceLocation CHAIN_TEXTURE = CosmicCore
            .id("textures/gui/ftbquests/dependency_lines/main_quest_line.png");

    private final InventoryScreen screen;

    DeedInventoryButton(InventoryScreen screen, Component message, OnPress onPress) {
        super(
                buttonX(screen),
                buttonY(screen),
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                message,
                onPress,
                DEFAULT_NARRATION);
        this.screen = screen;
    }

    public static int buttonX(InventoryScreen screen) {
        return screen.getGuiLeft() + (screen.getXSize() - BUTTON_WIDTH) / 2;
    }

    public static int buttonY(InventoryScreen screen) {
        return Math.min(
                screen.getGuiTop() + screen.getYSize() + 8,
                screen.height - BUTTON_HEIGHT - TEXTURE_BUFFER);
    }

    public static int visualBottom(InventoryScreen screen) {
        return buttonY(screen) + BUTTON_HEIGHT + TEXTURE_BUFFER;
    }

    public static boolean visibleOnScreen(InventoryScreen screen) {
        return screen.width >= 379 || !screen.getRecipeBookComponent().isVisible();
    }

    @Override
    public boolean isActive() {
        return visibleOnScreen(screen) && super.isActive();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return visibleOnScreen(screen) && super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent event) {
        return visibleOnScreen(screen) ? super.nextFocusPath(event) : null;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        setPosition(buttonX(screen), buttonY(screen));

        float brightness = isHoveredOrFocused() ? 1.0F : 0.86F;
        guiGraphics.setColor(brightness, brightness, brightness, alpha);
        guiGraphics.blit(
                BUTTON_TEXTURE,
                getX() - TEXTURE_BUFFER,
                getY() - TEXTURE_BUFFER,
                0,
                0,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        int textColor = isHoveredOrFocused() ? 0xFFFFE8A3 : 0xFFE4C16D;
        renderScrollingString(guiGraphics, Minecraft.getInstance().font, 4, textColor);
    }

    static void renderChains(GuiGraphics guiGraphics, InventoryScreen screen) {
        int inventoryBottom = screen.getGuiTop() + screen.getYSize();
        float buttonCenterX = buttonX(screen) + BUTTON_WIDTH / 2.0F;
        float buttonCenterY = buttonY(screen) + BUTTON_HEIGHT / 2.0F;
        guiGraphics.enableScissor(0, inventoryBottom, screen.width, screen.height);
        guiGraphics.setColor(1.0F, 0.84F, 0.28F, 1.0F);
        renderChain(
                guiGraphics,
                screen.getGuiLeft() + 4.0F,
                inventoryBottom - 7.0F,
                buttonCenterX - CHAIN_ANCHOR_OFFSET,
                buttonCenterY);
        renderChain(
                guiGraphics,
                screen.getGuiLeft() + screen.getXSize() - 4.0F,
                inventoryBottom - 7.0F,
                buttonCenterX + CHAIN_ANCHOR_OFFSET,
                buttonCenterY);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.disableScissor();
    }

    private static void renderChain(GuiGraphics guiGraphics, float startX, float startY, float endX, float endY) {
        float deltaX = endX - startX;
        float deltaY = endY - startY;
        float length = Mth.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (length <= 0.0F) return;

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(startX, startY, 0.0F);
        poseStack.mulPose(Axis.ZP.rotation((float) Math.atan2(deltaY, deltaX)));
        poseStack.scale(CHAIN_SCALE, CHAIN_SCALE, 1.0F);
        int rendered = 0;
        int targetLength = Mth.ceil(length / CHAIN_SCALE);
        while (rendered < targetLength) {
            int segmentWidth = Math.min(CHAIN_TILE_SIZE, targetLength - rendered);
            guiGraphics.blit(
                    CHAIN_TEXTURE,
                    rendered,
                    -CHAIN_TILE_SIZE / 2,
                    0,
                    0,
                    segmentWidth,
                    CHAIN_TILE_SIZE,
                    CHAIN_TILE_SIZE,
                    CHAIN_TILE_SIZE);
            rendered += segmentWidth;
        }
        poseStack.popPose();
    }
}
