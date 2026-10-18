package com.ghostipedia.cosmiccore.mixin.spacegravity.client;

import com.ghostipedia.cosmiccore.common.gravity.GravityManager;

import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import com.spacegravity.spacegravity.SpaceGravityClientModEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SpaceGravityClientModEvents.class, remap = false)
public abstract class SpaceGravityKeyMappingsMixin {

    @Inject(method = "onRegisterKeyMappings", at = @At("HEAD"), cancellable = true, require = 1)
    private static void cosmiccore$disableGravityControls(RegisterKeyMappingsEvent event, CallbackInfo callback) {
        if (!GravityManager.isEnabled()) callback.cancel();
    }
}
