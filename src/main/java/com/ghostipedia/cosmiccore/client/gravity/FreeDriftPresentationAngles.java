package com.ghostipedia.cosmiccore.client.gravity;

import com.ghostipedia.cosmiccore.common.firmament.FirmamentFreeDriftSteering;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import com.spacegravity.spacegravity.ZeroGravityOrientation;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class FreeDriftPresentationAngles {

    private static final double POLE_ENTER_HORIZONTAL_SQUARED = 1.0E-5;
    private static final double POLE_EXIT_HORIZONTAL_SQUARED = 4.0E-5;
    private static final Vec3 WORLD_UP = new Vec3(0.0, 1.0, 0.0);
    private static final Map<Player, PlayerState> STATES = Collections.synchronizedMap(new WeakHashMap<>());

    private FreeDriftPresentationAngles() {}

    public static ZeroGravityOrientation.CameraAngles stabilize(
                                                                Player player,
                                                                ZeroGravityOrientation.OrientationData orientation,
                                                                ZeroGravityOrientation.CameraAngles original) {
        if (!FirmamentFreeDriftSteering.isManaged(player)) {
            STATES.remove(player);
            return original;
        }
        synchronized (STATES) {
            PlayerState playerState = STATES.computeIfAbsent(player, ignored -> new PlayerState());
            int tick = player.tickCount;
            if (playerState.lastTick != tick && playerState.lastTick != tick - 1) {
                playerState.angles.reset();
            }
            playerState.lastTick = tick;
            return stabilize(orientation, original, playerState.angles);
        }
    }

    static ZeroGravityOrientation.CameraAngles stabilize(
                                                         ZeroGravityOrientation.OrientationData orientation,
                                                         ZeroGravityOrientation.CameraAngles original,
                                                         ContinuityState state) {
        ZeroGravityOrientation.OrientationData normalized = ZeroGravityOrientation.normalize(
                orientation.forward(), orientation.up());
        Vec3 forward = normalized.forward();
        double horizontalSquared = forward.x * forward.x + forward.z * forward.z;

        if (!state.initialized) {
            state.set(original.yaw(), original.pitch(), original.roll());
            state.poleLocked = horizontalSquared <= POLE_ENTER_HORIZONTAL_SQUARED;
            return original;
        }

        state.poleLocked = state.poleLocked ?
                horizontalSquared < POLE_EXIT_HORIZONTAL_SQUARED :
                horizontalSquared <= POLE_ENTER_HORIZONTAL_SQUARED;

        Candidate selected;
        if (state.poleLocked) {
            float yaw = state.yaw;
            float pitch = nearestEquivalent(polePitch(forward, yaw), state.pitch);
            float roll = nearestEquivalent(rollForYaw(normalized, yaw), state.roll);
            selected = new Candidate(yaw, pitch, roll);
        } else {
            Candidate canonical = new Candidate(
                    nearestEquivalent(original.yaw(), state.yaw),
                    nearestEquivalent(original.pitch(), state.pitch),
                    nearestEquivalent(original.roll(), state.roll));
            float alternatePitch = original.pitch() >= 0.0F ?
                    180.0F - original.pitch() :
                    -180.0F - original.pitch();
            Candidate alternate = new Candidate(
                    nearestEquivalent(original.yaw() + 180.0F, state.yaw),
                    nearestEquivalent(alternatePitch, state.pitch),
                    nearestEquivalent(original.roll() + 180.0F, state.roll));
            selected = canonical.distanceSquared(state) <= alternate.distanceSquared(state) ? canonical : alternate;
        }

        state.set(selected.yaw(), selected.pitch(), selected.roll());
        return new ZeroGravityOrientation.CameraAngles(selected.yaw(), selected.pitch(), selected.roll());
    }

    private static float polePitch(Vec3 forward, float yaw) {
        Vec3 horizontalForward = Vec3.directionFromRotation(0.0F, yaw);
        double horizontalProjection = forward.x * horizontalForward.x + forward.z * horizontalForward.z;
        return (float) Math.toDegrees(Math.atan2(-forward.y, horizontalProjection));
    }

    private static float rollForYaw(ZeroGravityOrientation.OrientationData orientation, float yaw) {
        Vec3 forward = orientation.forward();
        Vec3 up = orientation.up();
        Vec3 horizontalForward = Vec3.directionFromRotation(0.0F, yaw);
        Vec3 right = WORLD_UP.cross(horizontalForward).normalize();
        Vec3 baseUp = forward.cross(right).normalize();
        double sine = forward.dot(baseUp.cross(up));
        double cosine = Math.clamp(baseUp.dot(up), -1.0, 1.0);
        return Mth.wrapDegrees((float) Math.toDegrees(Math.atan2(sine, cosine)));
    }

    private static float nearestEquivalent(float angle, float reference) {
        return reference + Mth.wrapDegrees(angle - reference);
    }

    static final class ContinuityState {

        private boolean initialized;
        private boolean poleLocked;
        private float yaw;
        private float pitch;
        private float roll;

        private void set(float yaw, float pitch, float roll) {
            initialized = true;
            this.yaw = yaw;
            this.pitch = pitch;
            this.roll = roll;
        }

        private void reset() {
            initialized = false;
            poleLocked = false;
        }
    }

    private record Candidate(float yaw, float pitch, float roll) {

        private double distanceSquared(ContinuityState state) {
            double yawDelta = yaw - state.yaw;
            double pitchDelta = pitch - state.pitch;
            double rollDelta = roll - state.roll;
            return yawDelta * yawDelta + pitchDelta * pitchDelta + rollDelta * rollDelta;
        }
    }

    private static final class PlayerState {

        private final ContinuityState angles = new ContinuityState();
        private int lastTick = Integer.MIN_VALUE;
    }
}
