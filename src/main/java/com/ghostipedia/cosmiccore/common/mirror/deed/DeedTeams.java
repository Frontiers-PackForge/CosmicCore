package com.ghostipedia.cosmiccore.common.mirror.deed;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import org.jetbrains.annotations.Nullable;

public final class DeedTeams {

    private DeedTeams() {}

    public static String teamKey(ServerPlayer player) {
        if (ModList.get().isLoaded("ftbteams")) {
            String key = Ftb.teamKey(player);
            if (key != null) return key;
        }
        return player.getUUID().toString();
    }

    private static final class Ftb {

        @Nullable
        static String teamKey(ServerPlayer player) {
            return FTBTeamsAPI.api().getManager().getTeamForPlayer(player)
                    .map(team -> team.getId().toString()).orElse(null);
        }
    }
}
