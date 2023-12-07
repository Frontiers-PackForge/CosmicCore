package com.ghostipedia.CosmicCore.common.breath;

import fuzs.thinair.api.v1.AirQualityHelper;
import fuzs.thinair.api.v1.AirQualityLevel;
import fuzs.thinair.helper.AirQualityHelperImpl;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class OxygenHelper {


    public static boolean airQualityActivatesHelmet(LivingEntity entity) {
        final var air = AirQualityHelperImpl.INSTANCE.getAirQualityAtLocation(entity.level(),entity.getEyePosition());
        return air == AirQualityLevel.RED || air == AirQualityLevel.YELLOW;
    }


//
//    @SubscribeEvent
//    public static void aircheck(LivingEvent.LivingTickEvent event){
//        if(event.getEntity() instanceof Player player){
//
//        }
//
//    }



}
