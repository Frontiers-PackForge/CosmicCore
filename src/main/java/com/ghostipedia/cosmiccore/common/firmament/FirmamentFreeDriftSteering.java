package com.ghostipedia.cosmiccore.common.firmament;

import com.ghostipedia.cosmiccore.common.data.CosmicAttachmentTypes;
import com.ghostipedia.cosmiccore.common.dimension.FirmamentDimension;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import com.spacegravity.spacegravity.ZeroGravityInputState;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class FirmamentFreeDriftSteering {

    private static final double INPUT_EPSILON_SQUARED = 1.0E-8;
    private static final double NORMAL_CRUISE_SPEED = 0.65;
    private static final double BOOSTED_CRUISE_SPEED = 1.0;
    private static final double NORMAL_MAX_CORRECTION = 0.09;
    private static final double BOOSTED_MAX_CORRECTION = 0.14;
    private static final double INPUT_RAMP_PER_TICK = 0.2;
    private static final Map<Player, SteeringState> STATES = Collections.synchronizedMap(new WeakHashMap<>());

    private FirmamentFreeDriftSteering() {}

    public static Vec3 shapeThrust(Player player, ZeroGravityInputState input, Vec3 fallbackDelta) {
        if (!isManaged(player)) return fallbackDelta;
        Vec3 command = commandFromInput(input);
        double envelope = advanceEnvelope(player);
        return shapeDelta(player.getDeltaMovement(), command, input.boosted(), envelope);
    }

    public static boolean isManaged(Player player) {
        if (!player.level().dimension().equals(FirmamentDimension.KEY)) return false;
        if (FirmamentTraversalLogic.isManagedFreeDrift(player)) return true;
        FirmamentTraversalState state = player.getExistingDataOrNull(CosmicAttachmentTypes.FIRMAMENT_TRAVERSAL_STATE);
        return state != null && state.managedFreeDrift();
    }

    static Vec3 shapeDelta(Vec3 velocity, Vec3 command, boolean boosted, double envelope) {
        double commandLengthSqr = command.lengthSqr();
        if (commandLengthSqr <= INPUT_EPSILON_SQUARED) return Vec3.ZERO;
        double inputStrength = Math.min(1.0, Math.sqrt(commandLengthSqr));
        double response = Math.clamp(envelope, 0.0, 1.0) * inputStrength;
        if (response == 0.0) return Vec3.ZERO;

        Vec3 desiredDirection = command.scale(1.0 / Math.sqrt(commandLengthSqr));
        double cruiseSpeed = boosted ? BOOSTED_CRUISE_SPEED : NORMAL_CRUISE_SPEED;
        Vec3 delta = desiredDirection.scale(cruiseSpeed * inputStrength).subtract(velocity);
        double maximum = (boosted ? BOOSTED_MAX_CORRECTION : NORMAL_MAX_CORRECTION) * response;
        double deltaLengthSqr = delta.lengthSqr();
        if (deltaLengthSqr > maximum * maximum) {
            delta = delta.scale(maximum / Math.sqrt(deltaLengthSqr));
        }
        return delta;
    }

    static Vec3 commandFromInput(ZeroGravityInputState input) {
        Vec3 forward = new Vec3(input.forwardX(), input.forwardY(), input.forwardZ());
        if (forward.lengthSqr() <= INPUT_EPSILON_SQUARED) return Vec3.ZERO;
        forward = forward.normalize();

        Vec3 transmittedUp = new Vec3(input.upX(), input.upY(), input.upZ());
        Vec3 up = transmittedUp.subtract(forward.scale(transmittedUp.dot(forward)));
        if (up.lengthSqr() <= INPUT_EPSILON_SQUARED) {
            Vec3 reference = Math.abs(forward.y) < 0.9 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(0.0, 0.0, 1.0);
            up = reference.subtract(forward.scale(reference.dot(forward)));
        }
        up = up.normalize();
        Vec3 right = forward.cross(up).normalize();
        Vec3 command = forward.scale(input.forwardImpulse())
                .add(right.scale(-input.strafeImpulse()))
                .add(up.scale(input.verticalImpulse()));
        return command.lengthSqr() > 1.0 ? command.normalize() : command;
    }

    private static double advanceEnvelope(Player player) {
        synchronized (STATES) {
            SteeringState state = STATES.computeIfAbsent(player, ignored -> new SteeringState());
            int tick = player.tickCount;
            if (state.lastInputTick == tick) return state.envelope;
            if (state.lastInputTick != tick - 1) state.envelope = 0.0;
            state.envelope = Math.min(1.0, state.envelope + INPUT_RAMP_PER_TICK);
            state.lastInputTick = tick;
            return state.envelope;
        }
    }

    private static final class SteeringState {

        private int lastInputTick = Integer.MIN_VALUE;
        private double envelope;
    }
}
