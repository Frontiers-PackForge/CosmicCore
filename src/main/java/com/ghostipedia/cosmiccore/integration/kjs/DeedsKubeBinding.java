package com.ghostipedia.cosmiccore.integration.kjs;

import com.ghostipedia.cosmiccore.common.mirror.deed.DeedTeams;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedsAPI;
import com.ghostipedia.cosmiccore.common.recipe.condition.DeedCondition;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class DeedsKubeBinding {

    private DeedsKubeBinding() {}

    public static DeedBuilder register(String id) {
        return new DeedBuilder(id);
    }

    public static DeedCondition woven(String id) {
        return new DeedCondition(ResourceLocation.parse(id));
    }

    public static DeedCondition notWoven(String id) {
        return new DeedCondition(true, ResourceLocation.parse(id));
    }

    public static boolean grant(ServerPlayer player, String id) {
        return DeedsAPI.grantCoil(player, ResourceLocation.parse(id));
    }

    public static boolean isWoven(ServerPlayer player, String id) {
        var server = player.getServer();
        if (server == null) return false;
        return DeedsAPI.isWoven(server, DeedTeams.teamKey(player), ResourceLocation.parse(id));
    }
}
