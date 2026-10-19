package com.ghostipedia.cosmiccore.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({ LivingEntity.class, Player.class })
public abstract class FiniteAbsorptionMixin {

    @ModifyVariable(method = "internalSetAbsorptionAmount", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float cosmiccore$finiteAbsorption(float amount) {
        return Float.isFinite(amount) ? amount : 0.0f;
    }
}
