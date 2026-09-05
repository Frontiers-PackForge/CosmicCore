package com.ghostipedia.cosmiccore.mixin.client;

import com.ghostipedia.cosmiccore.client.renderer.transmission.PowerTowerWireClient;

import net.minecraft.client.renderer.LevelRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class PowerTowerWireLightingMixin {

    @Inject(method = "setSectionDirty(IIIZ)V", at = @At("HEAD"))
    private void cosmiccore$wireLighting(int x, int y, int z, boolean immediate, CallbackInfo callback) {
        PowerTowerWireClient.sectionDirty(x, y, z);
    }
}
