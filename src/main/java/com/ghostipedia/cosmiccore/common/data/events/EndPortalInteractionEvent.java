package com.ghostipedia.cosmiccore.common.data.events;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EndPortalInteractionEvent {

    @SubscribeEvent
    public static void denyRightClick(PlayerInteractEvent event) {
        var player = event.getEntity();
        var level = event.getLevel();
        var hand = event.getHand();
    }
}
