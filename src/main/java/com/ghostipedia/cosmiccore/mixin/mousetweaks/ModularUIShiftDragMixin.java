package com.ghostipedia.cosmiccore.mixin.mousetweaks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.client.event.ScreenEvent;

import brachy.modularui.core.mixins.client.AbstractContainerScreenAccessor;
import brachy.modularui.screen.ClientScreenHandler;
import brachy.modularui.widgets.slot.ModularSlot;
import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientScreenHandler.class, remap = false)
public class ModularUIShiftDragMixin {

    @Unique
    private static boolean cosmiccore$leftDrag;

    @Unique
    private static Slot cosmiccore$lastSlot;

    @Inject(method = "onScreenMousePressed", at = @At("HEAD"))
    private static void cosmiccore$beginShiftDrag(ScreenEvent.MouseButtonPressed.Pre event, CallbackInfo ci) {
        if (event.getButton() != InputConstants.MOUSE_BUTTON_LEFT) {
            return;
        }
        var container = cosmiccore$getContainerScreen();
        cosmiccore$leftDrag = container != null && container.getMenu().getCarried().isEmpty();
        cosmiccore$lastSlot = cosmiccore$leftDrag ? cosmiccore$getHoveredSlot(container) : null;
    }

    @Inject(method = "onScreenMouseDragged", at = @At("HEAD"))
    private static void cosmiccore$shiftDrag(ScreenEvent.MouseDragged.Pre event, CallbackInfo ci) {
        if (!cosmiccore$leftDrag || event.getMouseButton() != InputConstants.MOUSE_BUTTON_LEFT) {
            return;
        }
        var container = cosmiccore$getContainerScreen();
        if (container == null) {
            cosmiccore$resetShiftDrag();
            return;
        }
        Slot slot = cosmiccore$getHoveredSlot(container);
        if (slot == cosmiccore$lastSlot) {
            return;
        }
        cosmiccore$lastSlot = slot;
        if (!Screen.hasShiftDown() || slot == null || !slot.hasItem() || !slot.isActive() ||
                slot instanceof ModularSlot modularSlot && modularSlot.isPhantom()) {
            return;
        }
        var minecraft = Minecraft.getInstance();
        if (minecraft.gameMode != null && minecraft.player != null && slot.mayPickup(minecraft.player)) {
            minecraft.gameMode.handleInventoryMouseClick(container.getMenu().containerId, slot.index,
                    InputConstants.MOUSE_BUTTON_LEFT, ClickType.QUICK_MOVE, minecraft.player);
        }
    }

    @Inject(method = "onScreenMouseReleased", at = @At("HEAD"))
    private static void cosmiccore$endShiftDrag(ScreenEvent.MouseButtonReleased.Pre event, CallbackInfo ci) {
        if (event.getButton() == InputConstants.MOUSE_BUTTON_LEFT) {
            cosmiccore$resetShiftDrag();
        }
    }

    @Unique
    private static AbstractContainerScreen<?> cosmiccore$getContainerScreen() {
        var screen = ClientScreenHandler.getMuiScreen();
        if (screen == null ||
                !(screen.getScreenWrapper().wrappedScreen() instanceof AbstractContainerScreen<?> container)) {
            return null;
        }
        return container;
    }

    @Unique
    private static Slot cosmiccore$getHoveredSlot(AbstractContainerScreen<?> container) {
        return ((AbstractContainerScreenAccessor) container).getHoveredSlot();
    }

    @Unique
    private static void cosmiccore$resetShiftDrag() {
        cosmiccore$leftDrag = false;
        cosmiccore$lastSlot = null;
    }
}
