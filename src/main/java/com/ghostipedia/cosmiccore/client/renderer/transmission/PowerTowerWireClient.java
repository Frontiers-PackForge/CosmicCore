package com.ghostipedia.cosmiccore.client.renderer.transmission;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.item.PowerTowerCoilItem;
import com.ghostipedia.cosmiccore.common.network.packet.PowerTowerSpanPacket;
import com.ghostipedia.cosmiccore.common.transmission.geometry.PowerTowerWireGeometry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT)
public final class PowerTowerWireClient {

    private static final Map<UUID, Span> SPANS = new HashMap<>();
    private static final Map<Long, HashSet<UUID>> CHUNKS = new HashMap<>();
    private static Level world;

    public static void update(PowerTowerSpanPacket packet) {
        var level = Minecraft.getInstance().level;
        if (level == null || !level.dimension().location().equals(packet.dimension())) return;
        if (world != level) clear();
        world = level;
        Span old = SPANS.remove(packet.id());
        if (old != null) {
            old.close();
            CHUNKS.values().removeIf(ids -> {
                ids.remove(packet.id());
                return ids.isEmpty();
            });
        }
        if (packet.starts().isEmpty()) return;
        var geometry = new PowerTowerWireGeometry(packet.starts(), packet.ends());
        SPANS.put(packet.id(), new Span(geometry));
        var box = geometry.bounds();
        for (int x = (int) Math.floor(box.minX) >> 4; x <= (int) Math.floor(box.maxX) >> 4; x++) {
            for (int z = (int) Math.floor(box.minZ) >> 4; z <= (int) Math.floor(box.maxZ) >> 4; z++) {
                CHUNKS.computeIfAbsent(ChunkPos.asLong(x, z), ignored -> new HashSet<>()).add(packet.id());
            }
        }
    }

    public static List<PowerTowerWireGeometry> query(Level level, AABB box) {
        if (world != level) return List.of();
        var ids = new HashSet<UUID>();
        for (int x = (int) Math.floor(box.minX) >> 4; x <= (int) Math.floor(box.maxX) >> 4; x++) {
            for (int z = (int) Math.floor(box.minZ) >> 4; z <= (int) Math.floor(box.maxZ) >> 4; z++) {
                var chunk = CHUNKS.get(ChunkPos.asLong(x, z));
                if (chunk != null) ids.addAll(chunk);
            }
        }
        return ids.stream().map(SPANS::get).map(span -> span.geometry)
                .filter(geometry -> geometry.bounds().intersects(box)).toList();
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        var minecraft = Minecraft.getInstance();
        if (world != minecraft.level) return;
        int uploads = 0;
        for (Span span : SPANS.values()) {
            if (!event.getFrustum().isVisible(span.geometry.bounds())) continue;
            if ((span.mesh == null || span.dirty) && uploads < 2) {
                uploads++;
                span.close();
                span.mesh = new PowerTowerWireMesh(span.geometry);
                span.dirty = false;
            }
            if (span.mesh != null) span.mesh.render(event);
        }
        highlight(event);
    }

    private static void highlight(RenderLevelStageEvent event) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null || !(player.isHolding(stack -> stack.getItem() instanceof PowerTowerCoilItem) ||
                com.ghostipedia.cosmiccore.common.transmission.PowerTowerWireRide.hasWrench(player)))
            return;
        Vec3 start = event.getCamera().getPosition();
        Vec3 end = start.add(player.getViewVector(event.getPartialTick().getGameTimeDeltaPartialTick(true))
                .scale(player.blockInteractionRange()));
        end = world.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player))
                .getLocation();
        PowerTowerWireGeometry selected = null;
        double nearest = start.distanceToSqr(end);
        AABB rayBox = new AABB(start, end).inflate(0.15);
        for (var geometry : query(world, rayBox)) {
            for (var segment : geometry.query(rayBox)) {
                var hit = segment.bounds().inflate(0.05).clip(start, end);
                if (hit.isPresent() && start.distanceToSqr(hit.get()) < nearest) {
                    nearest = start.distanceToSqr(hit.get());
                    selected = geometry;
                }
            }
        }
        if (selected == null) return;
        var buffers = minecraft.renderBuffers().bufferSource();
        var consumer = buffers.getBuffer(RenderType.lines());
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(-start.x, -start.y, -start.z);
        for (var segment : selected.query(rayBox.inflate(1))) {
            LevelRenderer.renderLineBox(pose, consumer, segment.bounds(), 0.3f, 0.85f, 1.0f, 0.8f);
        }
        pose.popPose();
        buffers.endBatch(RenderType.lines());
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel() == world) clear();
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }

    public static void clearMeshes() {
        SPANS.values().forEach(Span::close);
    }

    public static void sectionDirty(int x, int y, int z) {
        var ids = CHUNKS.get(ChunkPos.asLong(x, z));
        if (ids == null) return;
        for (UUID id : ids) {
            Span span = SPANS.get(id);
            if (span.geometry.bounds().maxY >= y * 16 && span.geometry.bounds().minY < (y + 1) * 16)
                span.dirty = true;
        }
    }

    @EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class Resources {

        @SubscribeEvent
        public static void reload(ModelEvent.BakingCompleted event) {
            Minecraft.getInstance().execute(PowerTowerWireClient::clearMeshes);
        }
    }

    private static void clear() {
        clearMeshes();
        SPANS.clear();
        CHUNKS.clear();
        world = null;
    }

    private static final class Span implements AutoCloseable {

        private final PowerTowerWireGeometry geometry;
        private PowerTowerWireMesh mesh;
        private boolean dirty;

        private Span(PowerTowerWireGeometry geometry) {
            this.geometry = geometry;
        }

        @Override
        public void close() {
            if (mesh != null) mesh.close();
            mesh = null;
        }
    }
}
