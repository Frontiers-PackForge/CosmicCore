package com.ghostipedia.cosmiccore.common.mirror.deed;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.DeedSyncPacket;

import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class DeedsAPI {

    private DeedsAPI() {}

    public static boolean grantCoil(ServerPlayer player, ResourceLocation deedId) {
        MinecraftServer server = player.getServer();
        if (server == null) return false;
        if (deedId.equals(DeedRegistry.THE_ADDRESS.id())) return false;
        String teamKey = DeedTeams.teamKey(player);
        boolean changed = DeedLedger.get(server).grantCoil(teamKey, deedId);
        if (changed) {
            syncTeam(server, teamKey);
        }
        return changed;
    }

    @Nullable
    public static DeedLedger.WovenEcho weave(ServerPlayer player, ResourceLocation deedId, boolean bindPosition) {
        MinecraftServer server = player.getServer();
        if (server == null) return null;
        String teamKey = DeedTeams.teamKey(player);
        GlobalPos pos = bindPosition ? GlobalPos.of(player.level().dimension(), player.blockPosition()) : null;
        DeedLedger.WovenEcho echo = DeedLedger.get(server).weave(teamKey, deedId, player.getUUID(),
                player.level().getGameTime(), pos);
        if (echo != null) {
            syncTeam(server, teamKey);
        }
        return echo;
    }

    public static boolean isWoven(MinecraftServer server, String teamKey, ResourceLocation deedId) {
        return DeedLedger.get(server).isWoven(teamKey, deedId);
    }

    public static void syncTeam(MinecraftServer server, String teamKey) {
        DeedLedger ledger = DeedLedger.get(server);
        List<ResourceLocation> woven = new ArrayList<>();
        for (DeedLedger.WovenEcho echo : ledger.wovenOf(teamKey)) {
            woven.add(echo.deedId());
        }
        List<ResourceLocation> pending = new ArrayList<>(ledger.pendingOf(teamKey));
        DeedSyncPacket packet = new DeedSyncPacket(woven, pending);
        int sent = 0;
        for (ServerPlayer online : server.getPlayerList().getPlayers()) {
            if (DeedTeams.teamKey(online).equals(teamKey)) {
                CCoreNetwork.sendToPlayer(online, packet);
                sent++;
            }
        }
        CosmicCore.LOGGER.info("Deed sync team {}: {} woven {} pending -> {} players", teamKey,
                woven.size(), pending.size(), sent);
    }

    public static void syncPlayer(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        String teamKey = DeedTeams.teamKey(player);
        DeedLedger ledger = DeedLedger.get(server);
        List<ResourceLocation> woven = new ArrayList<>();
        for (DeedLedger.WovenEcho echo : ledger.wovenOf(teamKey)) {
            woven.add(echo.deedId());
        }
        CCoreNetwork.sendToPlayer(player,
                new DeedSyncPacket(woven, new ArrayList<>(ledger.pendingOf(teamKey))));
    }
}
