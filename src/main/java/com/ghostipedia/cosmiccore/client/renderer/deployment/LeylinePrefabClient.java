package com.ghostipedia.cosmiccore.client.renderer.deployment;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.deployment.LeylinePrefab;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.LeylinePrefabRequestPacket;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT)
public final class LeylinePrefabClient {

    private static final Map<UUID, LeylinePrefab> CACHE = new LinkedHashMap<>(32, 0.75f, true);
    private static final Map<UUID, CompletableFuture<LeylinePrefab>> PENDING = new LinkedHashMap<>();
    private static final Map<UUID, Long> UNAVAILABLE = new LinkedHashMap<>();
    private static UUID inFlight;
    private static long ticks;
    private static long sentAt;
    private static long nextRequest;
    private static int cachedBlocks;
    private static int session;
    private static boolean decoding;

    private LeylinePrefabClient() {}

    public static LeylinePrefab get(UUID id) {
        return request(id).getNow(null);
    }

    public static CompletableFuture<LeylinePrefab> request(UUID id) {
        if (id == null || Minecraft.getInstance().level == null)
            return CompletableFuture.completedFuture(null);
        var cached = CACHE.get(id);
        if (cached != null) return CompletableFuture.completedFuture(cached);
        if (UNAVAILABLE.getOrDefault(id, 0L) > ticks) return CompletableFuture.completedFuture(null);
        var pending = PENDING.get(id);
        if (pending != null) return pending;
        if (PENDING.size() >= 32) return CompletableFuture.completedFuture(null);
        pending = new CompletableFuture<>();
        PENDING.put(id, pending);
        return pending;
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().level == null) return;
        ticks++;
        if (inFlight != null && !decoding && ticks - sentAt >= 200) complete(inFlight, null);
        if (inFlight != null || ticks < nextRequest || PENDING.isEmpty()) return;
        inFlight = PENDING.keySet().iterator().next();
        sentAt = ticks;
        nextRequest = ticks + 6;
        CCoreNetwork.sendToServer(new LeylinePrefabRequestPacket(inFlight));
    }

    public static void receive(UUID id, byte[] payload) {
        if (!id.equals(inFlight) || decoding) return;
        if (payload.length == 0) {
            complete(id, null);
            return;
        }
        decoding = true;
        int requestedSession = session;
        CompletableFuture.supplyAsync(() -> {
            var prefab = LeylinePrefab.fromPayload(payload);
            if (!prefab.id().equals(id)) throw new IllegalArgumentException("Blueprint identity mismatch");
            return prefab;
        }, net.minecraft.Util.backgroundExecutor())
                .whenComplete((prefab, error) -> Minecraft.getInstance().execute(() -> {
                    if (session != requestedSession || !id.equals(inFlight)) return;
                    if (error != null) CosmicCore.LOGGER.warn("Unable to load leyline blueprint {}", id, error);
                    complete(id, prefab);
                }));
    }

    private static void complete(UUID id, LeylinePrefab prefab) {
        var future = PENDING.remove(id);
        inFlight = null;
        decoding = false;
        if (prefab != null) {
            CACHE.put(id, prefab);
            cachedBlocks += prefab.blocks().size();
            var iterator = CACHE.values().iterator();
            while (CACHE.size() > 32 || cachedBlocks > 131072) {
                cachedBlocks -= iterator.next().blocks().size();
                iterator.remove();
            }
        }
        if (prefab == null) {
            UNAVAILABLE.put(id, ticks + 600);
            while (UNAVAILABLE.size() > 64) UNAVAILABLE.remove(UNAVAILABLE.keySet().iterator().next());
        }
        if (future != null) future.complete(prefab);
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        var pending = List.copyOf(PENDING.values());
        PENDING.clear();
        CACHE.clear();
        UNAVAILABLE.clear();
        inFlight = null;
        ticks = sentAt = nextRequest = 0;
        cachedBlocks = 0;
        session++;
        decoding = false;
        pending.forEach(future -> future.complete(null));
    }
}
