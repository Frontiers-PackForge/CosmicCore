package com.ghostipedia.cosmiccore.common.firmament;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public record FirmamentTraversalState(
                                      double weight,
                                      double targetWeight,
                                      Phase phase,
                                      boolean managedFreeDrift,
                                      boolean residualGravity,
                                      long serverTick) {

    public static final double ENTER_RATE = 1.0 / 12.0;
    public static final double EXIT_RATE = 1.0 / 24.0;
    public static final int EXIT_ROLL_TICKS = 24;
    public static final FirmamentTraversalState INACTIVE = new FirmamentTraversalState(
            0.0,
            0.0,
            Phase.INACTIVE,
            false,
            false,
            0L);
    public static final StreamCodec<RegistryFriendlyByteBuf, FirmamentTraversalState> STREAM_CODEC = StreamCodec
            .ofMember(FirmamentTraversalState::encode, FirmamentTraversalState::new);

    public FirmamentTraversalState {
        weight = finiteUnit(weight);
        targetWeight = finiteUnit(targetWeight);
        phase = Objects.requireNonNull(phase);
        residualGravity = residualGravity && managedFreeDrift;
    }

    private FirmamentTraversalState(RegistryFriendlyByteBuf buffer) {
        this(
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readEnum(Phase.class),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readLong());
    }

    private void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeDouble(weight);
        buffer.writeDouble(targetWeight);
        buffer.writeEnum(phase);
        buffer.writeBoolean(managedFreeDrift);
        buffer.writeBoolean(residualGravity);
        buffer.writeLong(serverTick);
    }

    public double predictedWeight(long localTick) {
        return advance(weight, targetWeight, predictionSteps(localTick));
    }

    public long predictedTick(long localTick) {
        return serverTick + predictionSteps(localTick);
    }

    public static double advance(double weight, double targetWeight, long ticks) {
        double current = finiteUnit(weight);
        double target = finiteUnit(targetWeight);
        long elapsed = Math.max(0L, ticks);
        if (target > current) return Math.min(target, current + ENTER_RATE * elapsed);
        if (target < current) return Math.max(target, current - EXIT_RATE * elapsed);
        return current;
    }

    public static Phase phase(double weight, double targetWeight, boolean release) {
        if (release) return Phase.RELEASE;
        if (targetWeight > weight) return Phase.ENTERING;
        if (targetWeight < weight) return Phase.EXITING;
        return weight > 0.0 ? Phase.ACTIVE : Phase.RELEASE;
    }

    public static double smootherstep(double value) {
        double clamped = finiteUnit(value);
        return clamped * clamped * clamped * (clamped * (clamped * 6.0 - 15.0) + 10.0);
    }

    private long predictionSteps(long localTick) {
        return Math.clamp(localTick - serverTick, 0L, 8L);
    }

    private static double finiteUnit(double value) {
        if (!Double.isFinite(value)) return 0.0;
        return Math.clamp(value, 0.0, 1.0);
    }

    public enum Phase {
        INACTIVE,
        ENTERING,
        ACTIVE,
        EXITING,
        RELEASE
    }
}
