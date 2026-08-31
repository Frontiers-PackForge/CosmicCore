package com.ghostipedia.cosmiccore.common.mirror.deed;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.compat.ftbquests.DeedQuestCompatBridge;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

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
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().getGameTime() % 20 != 0) return;
        DeedsAPI.reconcilePatientZero(player);
    }

    static void reconcileAndSync(ServerPlayer player) {
        if (!DeedsAPI.reconcilePatientZero(player)) {
            DeedsAPI.syncPlayer(player);
            var server = player.getServer();
            if (server != null) DeedQuestCompatBridge.syncTeam(server, DeedTeams.teamKey(player));
        }
    }

    static void reconcileAfterTeamChange(ServerPlayer player) {
        var server = player.getServer();
        if (server == null) return;
        DeedLedger.get(server).clearPresentations(player.getUUID());
        reconcileAndSync(player);
    }
}
