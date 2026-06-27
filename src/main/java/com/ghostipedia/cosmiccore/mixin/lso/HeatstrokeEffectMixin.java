package com.ghostipedia.cosmiccore.mixin.lso;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import sfiomn.legendarysurvivaloverhaul.common.effects.HeatStrokeEffect;

@Mixin(value = HeatStrokeEffect.class)
public abstract class HeatstrokeEffectMixin extends MobEffect {

    protected HeatstrokeEffectMixin(MobEffectCategory category, int color) {
        super(MobEffectCategory.HARMFUL, 16756041);
    }

    @ModifyArg(method = "applyEffectTick",
               at = @At(
                        value = "INVOKE",
                        target = "Lsfiomn/legendarysurvivaloverhaul/api/ModDamageTypes;hyperthermia(Lnet/minecraft/world/entity/Entity;F)V"),
               index = 1)
    private float cosmiccore$changeDamageAmount(float amount, @Local(argsOnly = true) int amplifier) {
        return amount + amplifier;
    }

    // TODO(cosmiccore): the shouldApplyEffectTickThisTick tick-rate override (force 'time' = 50) was removed -
    // @ModifyVariable at that STORE proved flaky to target reliably. Re-add with a verified injection point
    // (slot index, or an @Inject HEAD that recomputes) once the client launches and it can be tested in-game.
}
