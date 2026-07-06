package com.ghostipedia.cosmiccore.common.mirror.deed;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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
    }
}
