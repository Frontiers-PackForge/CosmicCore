package com.ghostipedia.cosmiccore.common.mirror.deed;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.event.TeamEvent;
import dev.ftb.mods.ftbteams.data.PlayerTeam;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class DeedTeams {

    private DeedTeams() {}

    public static String teamKey(ServerPlayer player) {
        if (ModList.get().isLoaded("ftbteams")) {
            String key = Ftb.teamKey(player);
            if (key != null) return key;
        }
        return player.getUUID().toString();
    }

    public static List<ServerPlayer> onlineTeamMembers(ServerPlayer player) {
        if (ModList.get().isLoaded("ftbteams")) {
            List<ServerPlayer> members = Ftb.onlineMembers(player);
            if (members != null) return members;
        }
        return List.of(player);
    }

    public static Collection<UUID> teamMemberIds(ServerPlayer player) {
        if (ModList.get().isLoaded("ftbteams")) {
            Collection<UUID> members = Ftb.memberIds(player);
            if (members != null && !members.isEmpty()) return members;
        }
        return List.of(player.getUUID());
    }

    public static void registerEvents() {
        if (ModList.get().isLoaded("ftbteams")) {
            Ftb.registerEvents();
        }
    }

    private static final class Ftb {

        @Nullable
        static String teamKey(ServerPlayer player) {
            if (!FTBTeamsAPI.api().isManagerLoaded()) return null;
            return FTBTeamsAPI.api().getManager().getTeamForPlayer(player)
                    .map(team -> team.getId().toString()).orElse(null);
        }

        @Nullable
        static List<ServerPlayer> onlineMembers(ServerPlayer player) {
            if (!FTBTeamsAPI.api().isManagerLoaded()) return null;
            return FTBTeamsAPI.api().getManager().getTeamForPlayer(player)
                    .<List<ServerPlayer>>map(team -> new ArrayList<>(team.getOnlineMembers()))
                    .orElse(null);
        }

        @Nullable
        static Collection<UUID> memberIds(ServerPlayer player) {
            if (!FTBTeamsAPI.api().isManagerLoaded()) return null;
            return FTBTeamsAPI.api().getManager().getTeamForPlayer(player)
                    .<Collection<UUID>>map(team -> team instanceof PlayerTeam playerTeam ?
                            new ArrayList<>(playerTeam.getEffectiveTeam().getMembers()) :
                            new ArrayList<>(team.getMembers()))
                    .orElse(null);
        }

        static void registerEvents() {
            TeamEvent.PLAYER_LOGGED_IN.register(event -> DeedEvents.reconcileAndSync(event.getPlayer()));
            TeamEvent.PLAYER_CHANGED.register(event -> {
                ServerPlayer player = event.getPlayer();
                if (player != null) DeedEvents.reconcileAfterTeamChange(player);
            });
        }
    }
}
