package com.ghostipedia.cosmiccore.mixin.modularui;

import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.event.ScreenEvent;

import brachy.modularui.screen.ClientScreenHandler;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.viewport.GuiContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

/**
 * MUI2 3.3.0 onScreenMouseScrolled bails with {@code if (wx == 0) return;} where wx is the HORIZONTAL scroll delta, so
 * a normal vertical mouse wheel (deltaX 0, deltaY non-zero) never reaches any scroll widget. This re-routes the
 * vertical-only case correctly. Upstream bug; should be {@code wx == 0 && wy == 0}.
 */
@Mixin(value = ClientScreenHandler.class, remap = false)
public class ModularUIScrollFixMixin {

    @Shadow
    @Final
    private static GuiContext defaultContext;

    @Shadow
    private static ModularScreen currentScreen;

    @Shadow
    private static boolean validateGui(Screen screen) {
        throw new AssertionError();
    }

    @Shadow
    private static boolean doAction(ModularScreen muiScreen, Predicate<ModularScreen> action) {
        throw new AssertionError();
    }

    @Inject(method = "onScreenMouseScrolled", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cosmiccore$fixVerticalWheelScroll(ScreenEvent.MouseScrolled.Pre event, CallbackInfo ci) {
        double wx = event.getScrollDeltaX();
        double wy = event.getScrollDeltaY();
        if (wx != 0 || wy == 0) return;
        defaultContext.updateMouseWheel(wx, wy);
        if (validateGui(event.getScreen())) {
            currentScreen.getContext().updateMouseWheel(wx, wy);
        }
        if (doAction(currentScreen, ms -> ms.mouseScrolled(wx, wy))) {
            event.setCanceled(true);
        }
        ci.cancel();
    }
}
