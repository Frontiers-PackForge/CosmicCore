package com.ghostipedia.cosmiccore.common.transmission;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.PowerTowerRideRequestPacket.Action;
import com.ghostipedia.cosmiccore.common.network.packet.PowerTowerRideStatePacket;
import com.ghostipedia.cosmiccore.common.transmission.geometry.PowerTowerWireGeometry;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerSavedData;

import com.gregtechceu.gtceu.common.data.item.GTItemAbilities;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = CosmicCore.MOD_ID)
public final class PowerTowerWireRide {

    public static final double SPEED = 0.4;
    public static final double ACCELERATION = 0.05;
    public static final double HANG_HEIGHT = 2.1;
    private static final String GRAVITY_RESTORE = "cosmiccore:wire_ride_gravity";
    private static final TagKey<Item> WRENCH = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("c", "tools/wrench"));
    private static final Map<UUID, Ride> RIDERS = new HashMap<>();

    public static boolean isWrench(ItemStack stack) {
        return stack.is(WRENCH) || stack.canPerformAction(GTItemAbilities.WRENCH_DIG) ||
                stack.canPerformAction(GTItemAbilities.WRENCH_ROTATE);
    }

    public static boolean hasWrench(Player player) {
        return isWrench(player.getMainHandItem()) || isWrench(player.getOffhandItem());
    }

    public static void request(ServerPlayer player, Action action, InteractionHand hand) {
        if (action != Action.GRAB) {
            detach(player, action == Action.JUMP);
            return;
        }
        if (RIDERS.containsKey(player.getUUID()) || !isWrench(player.getItemInHand(hand)) || !player.isAlive() ||
                player.isSpectator() || player.isPassenger() || player.isFallFlying() || player.isSleeping())
            return;
        ServerLevel level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(player.blockInteractionRange()));
        end = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player))
                .getLocation();
        AABB search = new AABB(eye, end).inflate(0.15);
        var graph = PowerTowerSavedData.getOrCreate(level).graph();
        var ids = new HashSet<UUID>();
        for (int x = (int) Math.floor(search.minX) >> 4; x <= (int) Math.floor(search.maxX) >> 4; x++) {
            for (int z = (int) Math.floor(search.minZ) >> 4; z <= (int) Math.floor(search.maxZ) >> 4; z++) {
                ids.addAll(graph.spanIdsInChunk(ChunkPos.asLong(x, z)));
            }
        }
        Ride selected = null;
        double nearest = eye.distanceToSqr(end);
        for (UUID id : ids) {
            var geometry = graph.wireGeometry(id);
            if (geometry == null) continue;
            for (var segment : geometry.query(search)) {
                var hit = segment.bounds().inflate(0.05).clip(eye, end);
                if (hit.isEmpty() || eye.distanceToSqr(hit.get()) >= nearest) continue;
                nearest = eye.distanceToSqr(hit.get());
                double progress = geometry.closestProgress(segment.wire(), hit.get());
                selected = new Ride(level, id, geometry, segment.wire(), progress);
            }
        }
        if (selected == null) return;
        Vec3 target = selected.geometry.point(selected.wire, selected.progress).add(0, -HANG_HEIGHT, 0);
        if (!clearPath(player, target)) return;
        player.getPersistentData().putBoolean(GRAVITY_RESTORE, player.isNoGravity());
        player.setNoGravity(true);
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
        player.connection.teleport(target.x, target.y, target.z, player.getYRot(), player.getXRot());
        RIDERS.put(player.getUUID(), selected);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, selected.packet(player));
    }

    private static boolean clearPath(ServerPlayer player, Vec3 target) {
        Vec3 delta = target.subtract(player.position());
        int steps = Math.max(1, (int) Math.ceil(delta.length() / 0.2));
        for (int i = 1; i <= steps; i++) {
            AABB box = player.getBoundingBox().move(delta.scale((double) i / steps));
            if (!player.serverLevel().hasChunksAt((int) Math.floor(box.minX), (int) Math.floor(box.minZ),
                    (int) Math.floor(box.maxX), (int) Math.floor(box.maxZ)) ||
                    !player.level().noCollision(player, box))
                return false;
        }
        return true;
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Ride ride = RIDERS.get(player.getUUID());
        if (ride == null) return;
        var graph = PowerTowerSavedData.getOrCreate(player.serverLevel()).graph();
        var current = graph.wireGeometry(ride.span);
        if (ride.level != player.level() || current == null || current.wireCount() <= ride.wire ||
                !current.point(ride.wire, 0).equals(ride.geometry.point(ride.wire, 0)) ||
                !current.point(ride.wire, 1).equals(ride.geometry.point(ride.wire, 1)) || !player.isAlive() ||
                !hasWrench(player) || player.isPassenger() || player.isSpectator() || player.isFallFlying() ||
                player.isShiftKeyDown()) {
            detach(player, false);
            return;
        }
        Vec3 grip = player.position().add(0, HANG_HEIGHT, 0);
        Vec3 previous = ride.geometry.point(ride.wire, ride.progress);
        ride.allowance = Math.min(3, ride.allowance + SPEED + 0.05);
        double junctionDistance = transfer(ride, grip);
        double progress = ride.geometry.closestProgress(ride.wire, grip);
        Vec3 point = ride.geometry.point(ride.wire, progress);
        double distance = junctionDistance + point.distanceTo(ride.geometry.point(ride.wire, ride.progress));
        if (point.distanceToSqr(grip) > 0.16 || distance > ride.allowance) {
            detach(player, false);
            return;
        }
        ride.allowance -= distance;
        Vec3 movement = point.subtract(previous);
        ride.velocity = movement.length() > SPEED ? movement.normalize().scale(SPEED) : movement;
        ride.progress = progress;
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
    }

    private static double transfer(Ride ride, Vec3 grip) {
        var graph = PowerTowerSavedData.getOrCreate(ride.level).graph();
        var incoming = graph.span(ride.span);
        double best = ride.geometry.point(ride.wire, ride.geometry.closestProgress(ride.wire, grip))
                .distanceToSqr(grip);
        UUID selected = null;
        PowerTowerWireGeometry selectedGeometry = null;
        int selectedWire = 0;
        int selectedEnd = 0;
        double traveled = 0;
        Vec3 previous = ride.geometry.point(ride.wire, ride.progress);
        for (int end = 0; end <= 1; end++) {
            Vec3 junction = ride.geometry.point(ride.wire, end);
            double approach = previous.distanceTo(junction);
            if (approach > ride.allowance) continue;
            UUID node = end == 0 ? incoming.firstNodeId() : incoming.secondNodeId();
            for (UUID id : graph.spanIdsInChunk(ChunkPos.asLong((int) Math.floor(junction.x) >> 4,
                    (int) Math.floor(junction.z) >> 4))) {
                if (id.equals(ride.span)) continue;
                var span = graph.span(id);
                if (span == null || (!span.firstNodeId().equals(node) && !span.secondNodeId().equals(node))) continue;
                var geometry = graph.wireGeometry(id);
                if (geometry == null) continue;
                int exit = span.firstNodeId().equals(node) ? 0 : 1;
                for (int wire = 0; wire < geometry.wireCount(); wire++) {
                    if (geometry.point(wire, exit).distanceToSqr(junction) > 0.000001) continue;
                    Vec3 point = geometry.point(wire, geometry.closestProgress(wire, grip));
                    double error = point.distanceToSqr(grip);
                    if (error >= best || approach + point.distanceTo(junction) > ride.allowance) continue;
                    best = error;
                    selected = id;
                    selectedGeometry = geometry;
                    selectedWire = wire;
                    selectedEnd = exit;
                    traveled = approach;
                }
            }
        }
        if (selected == null) return 0;
        ride.span = selected;
        ride.geometry = selectedGeometry;
        ride.wire = selectedWire;
        ride.progress = selectedEnd;
        return traveled;
    }

    public static void detach(ServerPlayer player, boolean jump) {
        Ride ride = RIDERS.remove(player.getUUID());
        if (ride == null) return;
        restoreGravity(player);
        player.resetFallDistance();
        player.setDeltaMovement(ride.velocity.add(0, jump ? 0.42 : 0, 0));
        player.hurtMarked = true;
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new PowerTowerRideStatePacket(player.getUUID(), false, Vec3.ZERO, Vec3.ZERO, 0));
    }

    private static void restoreGravity(Player player) {
        if (player.getPersistentData().contains(GRAVITY_RESTORE)) {
            player.setNoGravity(player.getPersistentData().getBoolean(GRAVITY_RESTORE));
            player.getPersistentData().remove(GRAVITY_RESTORE);
        }
    }

    @SubscribeEvent
    public static void tracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer observer && event.getTarget() instanceof ServerPlayer player) {
            var ride = RIDERS.get(player.getUUID());
            CCoreNetwork.sendToPlayer(observer, ride != null ? ride.packet(player) :
                    new PowerTowerRideStatePacket(player.getUUID(), false, Vec3.ZERO, Vec3.ZERO, 0));
        }
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) detach(player, false);
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        restoreGravity(event.getEntity());
    }

    @SubscribeEvent
    public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) detach(player, false);
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        if (event.getOriginal() instanceof ServerPlayer player) detach(player, false);
        restoreGravity(event.getEntity());
    }

    @SubscribeEvent
    public static void stopping(ServerStoppingEvent event) {
        for (var player : event.getServer().getPlayerList().getPlayers()) detach(player, false);
        RIDERS.clear();
    }

    private static final class Ride {

        private final ServerLevel level;
        private UUID span;
        private PowerTowerWireGeometry geometry;
        private int wire;
        private double progress;
        private double allowance = 1;
        private Vec3 velocity = Vec3.ZERO;

        private Ride(ServerLevel level, UUID span, PowerTowerWireGeometry geometry, int wire, double progress) {
            this.level = level;
            this.span = span;
            this.geometry = geometry;
            this.wire = wire;
            this.progress = progress;
        }

        private PowerTowerRideStatePacket packet(ServerPlayer player) {
            return new PowerTowerRideStatePacket(player.getUUID(), true, geometry.point(wire, 0),
                    geometry.point(wire, 1), progress);
        }
    }
}
