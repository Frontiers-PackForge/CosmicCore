package com.ghostipedia.cosmiccore.common.data.temperature.attribute;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

import sfiomn.legendarysurvivaloverhaul.api.temperature.TemperatureUtil;

import java.util.UUID;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class FireResApplicator {

    public static final UUID heatResModifierID = UUID.fromString("9b3cd493-22a7-4b4b-921e-f0c740300a7f");
    public static final UUID coldResModifierID = UUID.fromString("9df8481a-c770-4ffd-a8c9-c61989922b44");

    @SubscribeEvent
    public static void onPotionEffect(MobEffectEvent.Added event) {
        if (event.getEntity() instanceof Player player &&
                event.getEffectInstance().getEffect() == MobEffects.FIRE_RESISTANCE) {
            TemperatureUtil.internal.addHeatResistanceModifier(player, 500.0, heatResModifierID);
        }
        if (event.getEntity() instanceof Player player &&
                event.getEffectInstance().getEffect() == MobEffects.WATER_BREATHING) {

        }
    }

    @SubscribeEvent
    public static void onPotionEffect(MobEffectEvent.Expired event) {
        if (event.getEntity() instanceof Player player) {
            if (event.getEffectInstance() != null &&
                    event.getEffectInstance().getEffect() == MobEffects.FIRE_RESISTANCE) {
                TemperatureUtil.internal.addHeatResistanceModifier(player, 0.0, heatResModifierID);
            }
        }
    }

    @SubscribeEvent
    public static void onPotionEffect(MobEffectEvent.Remove event) {
        if (event.getEntity() instanceof Player player) {
            if (event.getEffectInstance() != null &&
                    event.getEffectInstance().getEffect() == MobEffects.FIRE_RESISTANCE) {
                TemperatureUtil.internal.addHeatResistanceModifier(player, 0.0, heatResModifierID);
            }
        }
    }
}
