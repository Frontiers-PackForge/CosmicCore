package com.ghostipedia.cosmiccore.mixin.undergarden.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quek.undergarden.event.UndergardenClientEvents;

@Mixin(value = UndergardenClientEvents.class, remap = false)
public class UthericInfectionClientDisableMixin {

    @Inject(method = "lambda$registerOverlays$26", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cosmiccore$disableInfectionBar(GuiGraphics graphics, DeltaTracker deltaTracker,
                                                       CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "lambda$registerOverlays$27", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cosmiccore$disableInfectionVignette(GuiGraphics graphics, DeltaTracker deltaTracker,
                                                            CallbackInfo ci) {
        ci.cancel();
    }
}
