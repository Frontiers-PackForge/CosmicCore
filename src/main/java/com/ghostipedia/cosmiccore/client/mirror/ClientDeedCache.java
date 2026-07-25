package com.ghostipedia.cosmiccore.client.mirror;

import com.ghostipedia.cosmiccore.common.mirror.deed.DeedLedger;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedRegistry;

import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ClientDeedCache {

    private ClientDeedCache() {}

    private static volatile List<ResourceLocation> woven = List.of();
    private static volatile List<ResourceLocation> pending = List.of();
    private static final Map<ResourceLocation, ClientPresentation> presentations = new LinkedHashMap<>();

    public record ClientPresentation(ResourceLocation deedId, boolean forced, boolean live) {}

    public static synchronized void applySync(List<ResourceLocation> wovenIn, List<ResourceLocation> pendingIn,
                                              List<DeedLedger.Presentation> presentationsIn) {
        woven = List.copyOf(wovenIn);
        pending = List.copyOf(pendingIn);
        Map<ResourceLocation, ClientPresentation> next = new LinkedHashMap<>();
        for (DeedLedger.Presentation presentation : presentationsIn) {
            ClientPresentation current = presentations.get(presentation.deedId());
            next.put(presentation.deedId(), new ClientPresentation(presentation.deedId(), presentation.forced(),
                    current != null && current.live()));
        }
        presentations.clear();
        presentations.putAll(next);
    }

    public static List<ResourceLocation> woven() {
        return woven;
    }

    public static List<ResourceLocation> pending() {
        return pending;
    }

    public static synchronized List<ClientPresentation> presentations() {
        return List.copyOf(presentations.values());
    }

    public static synchronized boolean entryUnlocked() {
        return woven.contains(DeedRegistry.NETHER_PERMIT.id()) &&
                presentations.values().stream().noneMatch(ClientPresentation::forced);
    }

    public static synchronized boolean canOpen() {
        if (entryUnlocked() || !presentations.isEmpty()) return true;
        for (ResourceLocation id : pending) {
            if (!id.equals(DeedRegistry.THE_ADDRESS.id())) return true;
        }
        return false;
    }

    public static synchronized void markLive(ResourceLocation deedId) {
        ClientPresentation current = presentations.get(deedId);
        if (current != null) {
            presentations.put(deedId, new ClientPresentation(deedId, current.forced(), true));
        } else {
            presentations.put(deedId, new ClientPresentation(deedId, false, true));
        }
    }

    public static synchronized void acknowledge(ResourceLocation deedId) {
        presentations.remove(deedId);
    }

    public static synchronized void clear() {
        woven = List.of();
        pending = List.of();
        presentations.clear();
    }
}
