package com.ghostipedia.cosmiccore.mixin.lso;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import sfiomn.legendarysurvivaloverhaul.common.effects.HeatStrokeEffect;

@Mixin(value = HeatStrokeEffect.class)
public abstract class HeatstrokeEffectMixin extends MobEffect {

    protected HeatstrokeEffectMixin(MobEffectCategory category, int color) {
        super(MobEffectCategory.HARMFUL, 16756041);
    }

    @ModifyArg(method = "applyEffectTick",
               at = @At(
                        value = "INVOKE",
                        target = "Lnet/minecraft/world/entity/player/Player;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
               index = 1)
    private float cosmicCore$changeDamageAmount(float amount, @Local(argsOnly = true) int amplifier) {
        return amount + amplifier;
    }

    @ModifyVariable(method = "isDurationEffectTick", at = @At(value = "STORE"), ordinal = 2, name = "time")
    private int cosmicCore$modifyDamageIncrement(int time) {
        return 50;
    }
}
