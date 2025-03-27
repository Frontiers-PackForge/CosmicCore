package com.ghostipedia.cosmiccore.common.breath;

import net.minecraft.world.entity.LivingEntity;

import fuzs.thinair.api.v1.AirQualityLevel;
import fuzs.thinair.helper.AirQualityHelperImpl;

public class OxygenHelper {

    public static boolean airQualityActivatesHelmet(LivingEntity entity) {
        final var air = AirQualityHelperImpl.INSTANCE.getAirQualityAtLocation(entity.level(), entity.getEyePosition());
        return air == AirQualityLevel.RED || air == AirQualityLevel.YELLOW;
    }

    //
    // @SubscribeEvent
    // public static void aircheck(LivingEvent.LivingTickEvent event){
    // if(event.getEntity() instanceof Player player){
    //
    // }
    //
    // }
}
