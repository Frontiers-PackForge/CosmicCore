package com.ghostipedia.cosmiccore.common.mirror.deed;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class DeedEvents {

    private DeedEvents() {}

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DeedsAPI.syncPlayer(player);
        }
    }
}
