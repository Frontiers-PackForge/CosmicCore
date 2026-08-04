package com.ghostipedia.cosmiccore.common.firmament;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.gravity.GravityApi;
import com.ghostipedia.cosmiccore.api.gravity.GravityFrame;
import com.ghostipedia.cosmiccore.api.gravity.GravityMode;
import com.ghostipedia.cosmiccore.common.data.CosmicAttachmentTypes;
import com.ghostipedia.cosmiccore.common.data.worldgen.firmament.FirmamentMiddleBandLayout;
import com.ghostipedia.cosmiccore.common.dimension.FirmamentDimension;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = CosmicCore.MOD_ID)
public final class FirmamentTraversalLogic {

    private static final String MANAGED_GRAVITY = "cosmiccoreFirmamentManagedGravity";
    private static final double EXIT_THRESHOLD = 0.02;
    private static final int EXIT_CONFIRMATION_TICKS = 3;
    private static final GravityFrame FREE_DRIFT_FRAME = new GravityFrame(
            GravityMode.FREE_DRIFT,
            Direction.DOWN,
            0.0,
            CosmicCore.id("firmament/storm"),
            10,
            12,
            0.0,
            0L);
    private static final Map<ServerPlayer, AnomalyState> ANOMALY_STATES = new WeakHashMap<>();

    private FirmamentTraversalLogic() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTickPre(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        boolean inFirmament = player.level().dimension().equals(FirmamentDimension.KEY);
        if (!inFirmament) {
            releaseOutsideFirmament(serverPlayer, false);
            return;
        }
        boolean canTraverse = !player.isSpectator() && !player.getAbilities().flying &&
                !player.isPassenger() && !player.isDeadOrDying();
        if (!canTraverse) {
            releaseNow(serverPlayer);
            return;
        }

        double targetWeight = smootherstep(FirmamentMiddleBandLayout.stormEnvelope(player.getY()));
        GravityFrame frame = GravityApi.getFrame(serverPlayer);
        boolean managedFrame = isManagedFrame(frame);
        AnomalyState state = ANOMALY_STATES.get(serverPlayer);
        if (state == null && targetWeight == 0.0 && !managedFrame) {
            manageSpaceGravity(serverPlayer, false);
            publishInactive(serverPlayer);
            return;
        }
        if (state == null) {
            state = new AnomalyState();
            ANOMALY_STATES.put(serverPlayer, state);
        }
        double anomalyWeight = state.approach(targetWeight);
        boolean freeDriftAvailable = FirmamentSpaceGravityCompat.isAvailable();

        if (managedFrame && !freeDriftAvailable) {
            manageSpaceGravity(serverPlayer, false);
            GravityApi.reset(serverPlayer);
            frame = GravityApi.getFrame(serverPlayer);
            managedFrame = false;
        }

        if (freeDriftAvailable && !managedFrame && GravityApi.isNormal(serverPlayer) && anomalyWeight > 0.0) {
            GravityApi.requestFrame(serverPlayer, FREE_DRIFT_FRAME);
            frame = GravityApi.getFrame(serverPlayer);
            managedFrame = isManagedFrame(frame);
        }

        boolean replacesVanillaGravity = false;
        boolean managedRuntime = false;
        if (managedFrame) {
            replacesVanillaGravity = manageSpaceGravity(serverPlayer, true);
            managedRuntime = FirmamentSpaceGravityCompat.isEnabled(serverPlayer);
        } else {
            manageSpaceGravity(serverPlayer, false);
        }
        if (targetWeight == 0.0 && anomalyWeight <= EXIT_THRESHOLD) {
            state.incrementExitTicks();
            if (state.exitTicks() >= EXIT_CONFIRMATION_TICKS) {
                state.releaseAfterTick = true;
            }
        } else {
            state.clearExitTicks();
        }

        boolean residualGravity = managedRuntime && replacesVanillaGravity && player.isNoGravity();
        FirmamentTraversalState.Phase phase = FirmamentTraversalState.phase(
                anomalyWeight,
                targetWeight,
                state.releaseAfterTick || targetWeight == 0.0 && anomalyWeight == 0.0);
        long phaseTick = player.level().getGameTime();
        serverPlayer.setData(
                CosmicAttachmentTypes.FIRMAMENT_TRAVERSAL_STATE,
                new FirmamentTraversalState(
                        anomalyWeight,
                        targetWeight,
                        phase,
                        managedFrame && managedRuntime,
                        residualGravity,
                        phaseTick));

        if (FirmamentTraversalForces.apply(serverPlayer, anomalyWeight, phaseTick, residualGravity)) {
            player.hasImpulse = true;
        }
        if (managedFrame && managedRuntime) {
            player.fallDistance = 0.0f;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTickPost(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        AnomalyState state = ANOMALY_STATES.get(player);
        if (state != null && state.releaseAfterTick) releaseNow(player);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!(event.getOriginal() instanceof ServerPlayer original) ||
                !(event.getEntity() instanceof ServerPlayer replacement)) {
            return;
        }
        CompoundTag originalData = original.getPersistentData();
        if (!originalData.getBoolean(MANAGED_GRAVITY)) return;
        replacement.getPersistentData().putBoolean(MANAGED_GRAVITY, true);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player &&
                !player.level().dimension().equals(FirmamentDimension.KEY)) {
            releaseOutsideFirmament(player, true);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player &&
                !player.level().dimension().equals(FirmamentDimension.KEY)) {
            releaseOutsideFirmament(player, true);
        }
    }

    private static void releaseOutsideFirmament(ServerPlayer player, boolean forceSync) {
        CompoundTag data = player.getPersistentData();
        boolean managed = data.getBoolean(MANAGED_GRAVITY);
        boolean runtimeEnabled = FirmamentSpaceGravityCompat.isEnabled(player);
        FirmamentSpaceGravityCompat.clearAuthorityState(player);
        if (forceSync || managed || runtimeEnabled) {
            FirmamentSpaceGravityCompat.setRuntimeEnabled(player, false);
        }
        data.remove(MANAGED_GRAVITY);
        boolean managedFrame = isManagedFrame(GravityApi.getFrame(player));
        if (managedFrame) GravityApi.reset(player);
        ANOMALY_STATES.remove(player);
        publishInactive(player);
    }

    private static void releaseNow(ServerPlayer player) {
        boolean managedFrame = isManagedFrame(GravityApi.getFrame(player));
        manageSpaceGravity(player, false);
        if (managedFrame) GravityApi.reset(player);
        ANOMALY_STATES.remove(player);
        publishInactive(player);
    }

    private static boolean isManagedFrame(GravityFrame frame) {
        return frame.mode() == GravityMode.FREE_DRIFT && frame.sourceId().equals(FREE_DRIFT_FRAME.sourceId());
    }

    static boolean isManagedFreeDrift(Player player) {
        return player.level().dimension().equals(FirmamentDimension.KEY) &&
                isManagedFrame(GravityApi.getFrame(player));
    }

    private static boolean manageSpaceGravity(ServerPlayer player, boolean enabled) {
        CompoundTag data = player.getPersistentData();
        if (!FirmamentSpaceGravityCompat.isAvailable()) {
            if (!enabled) data.remove(MANAGED_GRAVITY);
            return false;
        }
        if (enabled) {
            data.putBoolean(MANAGED_GRAVITY, true);
            if (!FirmamentSpaceGravityCompat.isEnabled(player)) {
                FirmamentSpaceGravityCompat.setRuntimeEnabled(player, true);
            }
            return true;
        }
        if (data.getBoolean(MANAGED_GRAVITY) || FirmamentSpaceGravityCompat.isEnabled(player)) {
            FirmamentSpaceGravityCompat.setRuntimeEnabled(player, false);
        }
        data.remove(MANAGED_GRAVITY);
        return false;
    }

    public static void forceReset(ServerPlayer player) {
        FirmamentSpaceGravityCompat.setRuntimeEnabled(player, false);
        player.getPersistentData().remove(MANAGED_GRAVITY);
        GravityApi.reset(player);
        ANOMALY_STATES.remove(player);
        publishInactive(player);
    }

    private static void publishInactive(ServerPlayer player) {
        FirmamentTraversalState state = player.getExistingDataOrNull(
                CosmicAttachmentTypes.FIRMAMENT_TRAVERSAL_STATE);
        if (state != null && state.phase() != FirmamentTraversalState.Phase.INACTIVE) {
            player.setData(CosmicAttachmentTypes.FIRMAMENT_TRAVERSAL_STATE, FirmamentTraversalState.INACTIVE);
        }
    }

    private static double smootherstep(double value) {
        double clamped = Mth.clamp(value, 0.0, 1.0);
        return clamped * clamped * clamped * (clamped * (clamped * 6.0 - 15.0) + 10.0);
    }

    private static final class AnomalyState {

        private double weight;
        private int exitTicks;
        private boolean releaseAfterTick;

        private double approach(double target) {
            weight = FirmamentTraversalState.advance(weight, target, 1L);
            return weight;
        }

        private int exitTicks() {
            return exitTicks;
        }

        private void incrementExitTicks() {
            exitTicks++;
        }

        private void clearExitTicks() {
            exitTicks = 0;
        }
    }
}
