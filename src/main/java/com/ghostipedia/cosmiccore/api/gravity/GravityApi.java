package com.ghostipedia.cosmiccore.api.gravity;

import com.ghostipedia.cosmiccore.common.gravity.GravityManager;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class GravityApi {

    private GravityApi() {}

    public static GravityFrame getFrame(Player player) {
        return GravityManager.getFrame(player);
    }

    public static boolean requestFrame(ServerPlayer player, GravityFrame frame) {
        return GravityManager.setFrame(player, frame);
    }

    public static boolean reset(ServerPlayer player) {
        return GravityManager.reset(player);
    }

    public static boolean isNormal(Player player) {
        return GravityManager.isNormal(player);
    }

    public static boolean isFreeDrift(Player player) {
        return GravityManager.isFreeDrift(player);
    }
}
