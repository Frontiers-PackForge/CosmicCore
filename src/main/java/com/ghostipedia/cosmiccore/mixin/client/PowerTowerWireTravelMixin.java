package com.ghostipedia.cosmiccore.mixin.client;

import com.ghostipedia.cosmiccore.client.renderer.transmission.PowerTowerRideClient;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PowerTowerWireTravelMixin {

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$slideOnWire(Vec3 input, CallbackInfo callback) {
        if (PowerTowerRideClient.travel((Player) (Object) this, input)) callback.cancel();
    }
}
