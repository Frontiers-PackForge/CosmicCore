package com.ghostipedia.cosmiccore.common.data.events;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class EndPortalInteractionEvent {

    @SubscribeEvent
    public static void denyRightClick(PlayerInteractEvent.RightClickBlock event) {
        var player = event.getEntity();
        var level = event.getLevel();
        var hand = event.getHand();
    }
}
