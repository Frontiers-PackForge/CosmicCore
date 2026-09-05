package com.ghostipedia.cosmiccore.client.renderer.transmission;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.PowerTowerRideRequestPacket;
import com.ghostipedia.cosmiccore.common.network.packet.PowerTowerRideRequestPacket.Action;
import com.ghostipedia.cosmiccore.common.network.packet.PowerTowerRideStatePacket;
import com.ghostipedia.cosmiccore.common.transmission.PowerTowerWireRide;
import com.ghostipedia.cosmiccore.common.transmission.geometry.PowerTowerWireGeometry;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT)
public final class PowerTowerRideClient {

    private static final Set<UUID> RIDERS = new HashSet<>();
    private static LocalRide local;
    private static long nextGrab;

    public static boolean isRiding(UUID player) {
        return RIDERS.contains(player);
    }

    public static void update(PowerTowerRideStatePacket packet) {
        if (packet.active()) RIDERS.add(packet.player());
        else RIDERS.remove(packet.player());
        var player = Minecraft.getInstance().player;
        if (player == null || !player.getUUID().equals(packet.player())) return;
        local = packet.active() ?
                new LocalRide(new PowerTowerWireGeometry(List.of(packet.start()), List.of(packet.end())),
                        packet.progress()) :
                null;
    }

    @SubscribeEvent
    public static void use(InputEvent.InteractionKeyMappingTriggered event) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (!event.isUseItem() || player == null || minecraft.level == null || minecraft.screen != null ||
                !PowerTowerWireRide.isWrench(player.getItemInHand(event.getHand())))
            return;
        if (local != null) {
            event.setCanceled(true);
            event.setSwingHand(false);
            return;
        }
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(player.blockInteractionRange()));
        end = minecraft.level
                .clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player))
                .getLocation();
        AABB box = new AABB(start, end).inflate(0.15);
        boolean target = false;
        for (var geometry : PowerTowerWireClient.query(minecraft.level, box)) {
            for (var segment : geometry.query(box)) {
                if (segment.bounds().inflate(0.05).clip(start, end).isPresent()) {
                    target = true;
                    break;
                }
            }
        }
        if (!target) return;
        event.setCanceled(true);
        event.setSwingHand(false);
        if (player.tickCount < nextGrab) return;
        nextGrab = player.tickCount + 5;
        CCoreNetwork.sendToServer(new PowerTowerRideRequestPacket(Action.GRAB, event.getHand()));
    }

    @SubscribeEvent
    public static void input(MovementInputUpdateEvent event) {
        if (local == null) return;
        var input = event.getInput();
        if (input.jumping || input.shiftKeyDown || !PowerTowerWireRide.hasWrench(event.getEntity())) {
            release(input.jumping ? Action.JUMP : Action.DROP);
            input.jumping = false;
        }
    }

    public static boolean travel(Player player, Vec3 input) {
        if (player != Minecraft.getInstance().player || local == null) return false;
        var ride = local;
        Vec3 tangent = ride.geometry.tangent(0, ride.progress).normalize();
        Vec3 look = player.getLookAngle();
        double facing = tangent.x * look.x + tangent.z * look.z;
        if (Math.abs(facing) > 0.05) ride.direction = facing < 0 ? -1 : 1;
        double targetSpeed = input.z * ride.direction * PowerTowerWireRide.SPEED;
        ride.speed += Math.clamp(targetSpeed - ride.speed, -PowerTowerWireRide.ACCELERATION,
                PowerTowerWireRide.ACCELERATION);
        double progress = ride.geometry.advance(0, ride.progress, ride.speed);
        boolean transferred = false;
        if ((progress == 0 || progress == 1) && Math.abs(ride.speed) > 0.0001) {
            double remaining = Math.max(0, Math.abs(ride.speed) -
                    ride.geometry.point(0, ride.progress).distanceTo(ride.geometry.point(0, progress)));
            if (transfer(player, ride, progress, tangent.scale(Math.signum(ride.speed)))) {
                transferred = true;
                progress = ride.geometry.advance(0, ride.progress, Math.copySign(remaining, ride.speed));
            }
        }
        Vec3 target = ride.geometry.point(0, progress).add(0, -PowerTowerWireRide.HANG_HEIGHT, 0);
        Vec3 motion = target.subtract(player.position());
        if (motion.lengthSqr() > 2.25 ||
                !player.level().hasChunksAt((int) Math.floor(target.x - 1), (int) Math.floor(target.z - 1),
                        (int) Math.floor(target.x + 1), (int) Math.floor(target.z + 1))) {
            release(Action.DROP);
            return false;
        }
        player.setDeltaMovement(motion);
        player.move(MoverType.SELF, motion);
        player.resetFallDistance();
        player.setDeltaMovement(Vec3.ZERO);
        if (player.position().distanceToSqr(target) > 0.0025) {
            release(Action.DROP);
            return true;
        }
        ride.progress = progress;
        if (!transferred && (progress == 0 || progress == 1)) ride.speed = 0;
        return true;
    }

    private static void release(Action action) {
        var player = Minecraft.getInstance().player;
        if (local == null || player == null) return;
        Vec3 momentum = local.geometry.tangent(0, local.progress).normalize().scale(local.speed);
        player.setDeltaMovement(momentum.add(0, action == Action.JUMP ? 0.42 : 0, 0));
        local = null;
        RIDERS.remove(player.getUUID());
        nextGrab = player.tickCount + 5;
        CCoreNetwork.sendToServer(new PowerTowerRideRequestPacket(action, InteractionHand.MAIN_HAND));
    }

    private static boolean transfer(Player player, LocalRide ride, double endpoint, Vec3 heading) {
        Vec3 junction = ride.geometry.point(0, endpoint);
        Vec3 previous = ride.geometry.point(0, 1 - endpoint);
        PowerTowerWireGeometry selected = null;
        int selectedEnd = 0;
        double best = -0.25;
        for (var geometry : PowerTowerWireClient.query(player.level(), new AABB(junction, junction).inflate(0.1))) {
            for (int wire = 0; wire < geometry.wireCount(); wire++) {
                for (int end = 0; end <= 1; end++) {
                    if (geometry.point(wire, end).distanceToSqr(junction) > 0.000001 ||
                            geometry.point(wire, 1 - end).distanceToSqr(previous) < 0.000001)
                        continue;
                    double alignment = geometry.tangent(wire, end).normalize().scale(end == 0 ? 1 : -1)
                            .dot(heading);
                    if (alignment <= best) continue;
                    best = alignment;
                    selected = new PowerTowerWireGeometry(List.of(geometry.point(wire, 0)),
                            List.of(geometry.point(wire, 1)));
                    selectedEnd = end;
                }
            }
        }
        if (selected == null) return false;
        int sign = selectedEnd == 0 ? 1 : -1;
        ride.direction *= sign * (ride.speed < 0 ? -1 : 1);
        ride.speed = Math.abs(ride.speed) * sign;
        ride.geometry = selected;
        ride.progress = selectedEnd;
        return true;
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) clear();
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }

    private static void clear() {
        local = null;
        RIDERS.clear();
        nextGrab = 0;
    }

    private static final class LocalRide {

        private PowerTowerWireGeometry geometry;
        private double progress;
        private double speed;
        private int direction = 1;

        private LocalRide(PowerTowerWireGeometry geometry, double progress) {
            this.geometry = geometry;
            this.progress = progress;
        }
    }
}
