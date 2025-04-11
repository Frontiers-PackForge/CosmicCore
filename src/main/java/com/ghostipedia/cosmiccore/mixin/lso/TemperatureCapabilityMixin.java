package com.ghostipedia.cosmiccore.mixin.lso;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import sfiomn.legendarysurvivaloverhaul.api.temperature.ITemperatureCapability;
import sfiomn.legendarysurvivaloverhaul.api.temperature.TemperatureEnum;
import sfiomn.legendarysurvivaloverhaul.api.temperature.TemperatureUtil;
import sfiomn.legendarysurvivaloverhaul.api.thirst.ThirstUtil;
import sfiomn.legendarysurvivaloverhaul.common.capabilities.temperature.TemperatureCapability;
import sfiomn.legendarysurvivaloverhaul.common.effects.FrostbiteEffect;
import sfiomn.legendarysurvivaloverhaul.common.effects.HeatStrokeEffect;
import sfiomn.legendarysurvivaloverhaul.config.Config;
import sfiomn.legendarysurvivaloverhaul.registry.MobEffectRegistry;

@Mixin(value = TemperatureCapability.class,remap = false)
public abstract class TemperatureCapabilityMixin implements ITemperatureCapability {


    /**
     * @author Ghostipedia
     * @reason digging my stupid hands into this so i can reset temp values
     */
    @Overwrite
    private void applyDangerousEffects(Player player, TemperatureEnum tempEnum) {
        if (Config.Baked.dangerousHeatTemperature && ThirstUtil.isThirstActive(player) && tempEnum == TemperatureEnum.HEAT_STROKE) {
            if (TemperatureEnum.HEAT_STROKE.getMiddle() <= getTemperatureLevel() && !HeatStrokeEffect.playerIsImmuneToHeat(player)) {
                // Apply hyperthermia
                if (!player.hasEffect(MobEffectRegistry.HEAT_STROKE.get()))
                    player.addEffect(new MobEffectInstance(MobEffectRegistry.HEAT_STROKE.get(), -1, 0, false, true));
                return;
            }
        } else if (Config.Baked.dangerousColdTemperature && tempEnum == TemperatureEnum.FROSTBITE) {
            if (TemperatureEnum.FROSTBITE.getMiddle() >= getTemperatureLevel() && !FrostbiteEffect.playerIsImmuneToFrost(player)) {
                // Apply hypothermia.json
                if (!player.hasEffect(MobEffectRegistry.FROSTBITE.get()))
                    player.addEffect(new MobEffectInstance(MobEffectRegistry.FROSTBITE.get(), -1, 0, false, true));
                return;
            }
        }
        if (player.hasEffect(MobEffectRegistry.HEAT_STROKE.get()))
            player.removeEffect(MobEffectRegistry.HEAT_STROKE.get());
        if (player.hasEffect(MobEffectRegistry.FROSTBITE.get()))
            player.removeEffect(MobEffectRegistry.FROSTBITE.get());
    }

}
