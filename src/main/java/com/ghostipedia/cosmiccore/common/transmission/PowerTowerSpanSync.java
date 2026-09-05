package com.ghostipedia.cosmiccore.common.transmission;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.PowerTowerSpanPacket;
import com.ghostipedia.cosmiccore.common.transmission.geometry.PowerTowerWireGeometry;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerSavedData;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = CosmicCore.MOD_ID)
public final class PowerTowerSpanSync {

    private static final Map<ServerPlayer, Viewer> VIEWERS = new HashMap<>();
    private static final Set<ServerPlayer> PENDING = new HashSet<>();

    public static void changed(ServerLevel level) {
        VIEWERS.forEach((player, viewer) -> {
            if (viewer.level == level) PENDING.add(player);
        });
    }

    @SubscribeEvent
    public static void watch(ChunkWatchEvent.Sent event) {
        var viewer = VIEWERS.computeIfAbsent(event.getPlayer(), ignored -> new Viewer(event.getLevel()));
        if (viewer.level != event.getLevel()) {
            viewer = new Viewer(event.getLevel());
            VIEWERS.put(event.getPlayer(), viewer);
        }
        viewer.chunks.add(event.getPos().toLong());
        PENDING.add(event.getPlayer());
    }

    @SubscribeEvent
    public static void unwatch(ChunkWatchEvent.UnWatch event) {
        var viewer = VIEWERS.get(event.getPlayer());
        if (viewer != null && viewer.level == event.getLevel()) {
            viewer.chunks.remove(event.getPos().toLong());
            PENDING.add(event.getPlayer());
        }
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        for (ServerPlayer player : PENDING) {
            var viewer = VIEWERS.get(player);
            if (viewer == null || player.serverLevel() != viewer.level) continue;
            var graph = PowerTowerSavedData.getOrCreate(viewer.level).graph();
            Set<UUID> visible = new HashSet<>();
            for (long chunk : viewer.chunks) visible.addAll(graph.spanIdsInChunk(chunk));
            var dimension = viewer.level.dimension().location();
            for (UUID id : visible) {
                var span = graph.span(id);
                if (span == null) continue;
                var endpoints = PowerTowerWireGeometry.endpoints(graph.node(span.firstNodeId()),
                        graph.node(span.secondNodeId()));
                var packet = new PowerTowerSpanPacket(dimension, id, span.cableVoltageTier(), endpoints.starts(),
                        endpoints.ends());
                if (!packet.equals(viewer.sent.put(id, packet))) CCoreNetwork.sendToPlayer(player, packet);
            }
            viewer.sent.keySet().removeIf(id -> {
                if (visible.contains(id)) return false;
                CCoreNetwork.sendToPlayer(player, new PowerTowerSpanPacket(dimension, id, 0, List.of(), List.of()));
                return true;
            });
        }
        PENDING.clear();
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        VIEWERS.remove(event.getEntity());
        PENDING.remove(event.getEntity());
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        VIEWERS.remove(event.getOriginal());
        PENDING.remove(event.getOriginal());
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        VIEWERS.clear();
        PENDING.clear();
    }

    private static final class Viewer {

        private final ServerLevel level;
        private final Set<Long> chunks = new HashSet<>();
        private final Map<UUID, PowerTowerSpanPacket> sent = new HashMap<>();

        private Viewer(ServerLevel level) {
            this.level = level;
        }
    }
}
