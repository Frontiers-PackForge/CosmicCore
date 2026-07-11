package com.ghostipedia.cosmiccore.common.compat.hooked;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;

import dev.thecodewarrior.hooked.hook.Hook;
import dev.thecodewarrior.hooked.platform.HookedPlatformCommon;

public final class HookedCompat {

    private HookedCompat() {}

    private static final boolean LOADED = ModList.get().isLoaded("hooked");

    public static boolean hookActive(ServerPlayer player) {
        return LOADED && anyHookLive(player);
    }

    private static boolean anyHookLive(ServerPlayer player) {
        var data = HookedPlatformCommon.getInstance().getHookedPlayerData(player);
        for (Hook hook : data.getHooks().values()) {
            if (hook.getState() != Hook.State.REMOVED) return true;
        }
        return false;
    }
}
