package com.ghostipedia.cosmiccore.mixin.emi;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import dev.emi.emi.registry.EmiExclusionAreas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

/**
 * Guards against EMI bug where getExclusion is called with null screen during reload.
 * This causes NPE: Cannot read field "f_96544_" because "screen" is null
 */
@Mixin(value = EmiExclusionAreas.class, remap = false)
public class EmiExclusionAreasMixin {

    @Inject(method = "getExclusion", at = @At("HEAD"), cancellable = true)
    private static void cosmiccore$guardGetExclusion(Screen screen, CallbackInfoReturnable<List<?>> cir) {
        // Guard against null screen passed during EMI reload
        if (screen == null) {
            cir.setReturnValue(Collections.emptyList());
            return;
        }
        // Also check if the screen's minecraft field is null
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            cir.setReturnValue(Collections.emptyList());
        }
    }
}
