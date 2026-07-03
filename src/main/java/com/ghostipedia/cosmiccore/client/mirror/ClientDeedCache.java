package com.ghostipedia.cosmiccore.client.mirror;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class ClientDeedCache {

    private ClientDeedCache() {}

    private static volatile List<ResourceLocation> woven = List.of();
    private static volatile List<ResourceLocation> pending = List.of();

    public static void applySync(List<ResourceLocation> wovenIn, List<ResourceLocation> pendingIn) {
        woven = List.copyOf(wovenIn);
        pending = List.copyOf(pendingIn);
    }

    public static List<ResourceLocation> woven() {
        return woven;
    }

    public static List<ResourceLocation> pending() {
        return pending;
    }

    public static void clear() {
        woven = List.of();
        pending = List.of();
    }
}
