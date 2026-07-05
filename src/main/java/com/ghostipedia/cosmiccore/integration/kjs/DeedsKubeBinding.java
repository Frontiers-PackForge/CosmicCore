package com.ghostipedia.cosmiccore.integration.kjs;

import com.ghostipedia.cosmiccore.common.mirror.deed.Deed;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedRegistry;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedTeams;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedsAPI;
import com.ghostipedia.cosmiccore.common.recipe.condition.DeedCondition;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;

public final class DeedsKubeBinding {

    private DeedsKubeBinding() {}

    public static Deed register(String id, String lever, int tier) {
        return register(id, lever, tier, "tier" + tier);
    }

    public static Deed register(String id, String lever, int tier, String chapter) {
        ResourceLocation rid = ResourceLocation.parse(id);
        String nameKey = "deed." + rid.getNamespace() + "." + rid.getPath().replace('/', '.');
        return DeedRegistry.put(
                new Deed(rid, nameKey, Deed.Lever.valueOf(lever.toUpperCase(Locale.ROOT)), tier, chapter));
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
