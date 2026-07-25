package com.ghostipedia.cosmiccore.common.mirror.deed;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = CosmicCore.MOD_ID)
public final class DeedEvents {

    static {
        DeedTeams.registerEvents();
    }

    private DeedEvents() {}

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!ModList.get().isLoaded("ftbteams") && event.getEntity() instanceof ServerPlayer player) {
            reconcileAndSync(player);
        }
    }

    @SubscribeEvent
    public static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player &&
                event.getAdvancement().id().equals(CosmicCore.id("nether_permit"))) {
            DeedsAPI.forcePatientZero(player);
        }
    }

    static void reconcileAndSync(ServerPlayer player) {
        var server = player.getServer();
        if (server == null) return;
        var advancement = server.getAdvancements().get(CosmicCore.id("nether_permit"));
        if (advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone()) {
            DeedsAPI.forcePatientZero(player);
        }
        DeedsAPI.syncPlayer(player);
    }

    static void reconcileAfterTeamChange(ServerPlayer player) {
        var server = player.getServer();
        if (server == null) return;
        DeedLedger.get(server).clearPresentations(player.getUUID());
        reconcileAndSync(player);
    }
}
