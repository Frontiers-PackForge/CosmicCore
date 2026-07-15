package com.ghostipedia.cosmiccore.mixin.ftbchunks;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.ftb.mods.ftbchunks.client.FTBChunksClient", remap = false)
public class FTBChunksDisabledMinimapHudMixin {

    @Inject(method = "renderHud", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$skipDisabledMinimap(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!FTBChunksClientConfigAccessor.cosmiccore$getMinimapEnabled().get()) {
            ci.cancel();
        }
    }
}
