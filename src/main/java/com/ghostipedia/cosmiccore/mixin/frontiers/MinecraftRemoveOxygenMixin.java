package com.ghostipedia.cosmiccore.mixin.frontiers;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Disables vanilla Minecraft's air/drowning system for players.
 * CosmicCore's oxygen system handles all breathing mechanics instead.
 */
@Mixin(LivingEntity.class)
public class MinecraftRemoveOxygenMixin {

    @Inject(method = "decreaseAirSupply", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$noPlayerAirDecrease(int currentAir, CallbackInfoReturnable<Integer> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof Player) {
            cir.setReturnValue(self.getMaxAirSupply()); // stays full
        }
    }

    @Inject(method = "increaseAirSupply", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$noPlayerAirIncrease(int currentAir, CallbackInfoReturnable<Integer> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof Player) {
            cir.setReturnValue(self.getMaxAirSupply()); // stays full
        }
    }
}
