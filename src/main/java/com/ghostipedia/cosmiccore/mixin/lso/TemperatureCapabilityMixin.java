package com.ghostipedia.cosmiccore.mixin.lso;

import com.ghostipedia.cosmiccore.utils.NumberUtils;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import sfiomn.legendarysurvivaloverhaul.api.temperature.ITemperatureCapability;
import sfiomn.legendarysurvivaloverhaul.api.temperature.TemperatureEnum;
import sfiomn.legendarysurvivaloverhaul.api.temperature.TemperatureUtil;
import sfiomn.legendarysurvivaloverhaul.common.capabilities.temperature.TemperatureCapability;

@Mixin(value = TemperatureCapability.class, remap = false)
public abstract class TemperatureCapabilityMixin implements ITemperatureCapability {

    @Shadow
    private float targetTemp;

    @Shadow
    public abstract float getTemperatureLevel();

    @ModifyExpressionValue(method = "applyDangerousEffects",
                           at = @At(value = "NEW", target = "net/minecraft/world/effect/MobEffectInstance")
    )
    public MobEffectInstance cosmiccore$modifyDangerousEffects(MobEffectInstance effect, Player player,
                                                               TemperatureEnum tempEnum) {
        /*
        // clamp target temp to the range [0,40] (+1 so that we don't divide by 0)
        float clampedTarget = TemperatureUtil.clampTemperature(targetTemp) + 1f;
        // do the same to the current temperature
        float clampedTemp = TemperatureUtil.clampTemperature(getTemperatureLevel()) + 1f;
        // divide current by target, or vice versa, depending on which is smaller.
        float small = Math.min(clampedTemp, clampedTarget);
        float large = Math.max(clampedTemp, clampedTarget);

        float percentOfTarget = Mth.positiveModulo(small / large, 1.0333333f);
        */

        // I think this should work? if not, try the commented out code above
        // If that doesn't work out either, ping screret
        float percentOfTarget = NumberUtils.mapRange(getTemperatureLevel(), tempEnum.getLowerBound(),
                tempEnum.getUpperBound(), 0f, 1f);

        // Use 5*distance as our effect amplifier.
        // This should make it larger the closer we get to percentOfTarget == 1.
        int amplifier = Math.round(percentOfTarget * 5f);
        return new MobEffectInstance(effect.getEffect(), effect.getDuration(), effect.getAmplifier() + amplifier);
    }

}
