package com.ghostipedia.cosmiccore.api.misc;

import com.gregtechceu.gtceu.api.GTValues;

import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.IntPredicate;

public final class DroneStationServiceLogic {

    public static final int INTERFACE_TIER = GTValues.HV;

    private DroneStationServiceLogic() {}

    public enum DroneTier {

        PLASMATIC(GTValues.ZPM, 4096, 6),
        SANGUINE(GTValues.LuV, 2048, 5),
        INDUSTRIAL(GTValues.IV, 1024, 4),
        ROBUST(GTValues.EV, 512, 3),
        RUSTY(GTValues.HV, 256, 2);

        private final int voltageTier;
        private final int range;
        private final int usesPerDrone;

        DroneTier(int voltageTier, int range, int usesPerDrone) {
            this.voltageTier = voltageTier;
            this.range = range;
            this.usesPerDrone = usesPerDrone;
        }

        public int voltageTier() {
            return voltageTier;
        }

        public long voltage() {
            return GTValues.V[voltageTier];
        }

        public int range() {
            return range;
        }

        public int usesPerDrone() {
            return usesPerDrone;
        }

        public String translationKey() {
            return "cosmiccore.multiblock.drone_station.tier." + name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    public static DroneTier selectHighest(IntPredicate hasPartialUses, IntPredicate hasFreshDrone) {
        for (DroneTier tier : DroneTier.values()) {
            int ordinal = tier.ordinal();
            if (hasPartialUses.test(ordinal) || hasFreshDrone.test(ordinal)) return tier;
        }
        return null;
    }

    public static UseResult consumeUse(int remainingUses, int usesPerDrone, boolean freshDroneAvailable) {
        return consumeUse(remainingUses, usesPerDrone, () -> freshDroneAvailable);
    }

    public static UseResult consumeUse(int remainingUses, int usesPerDrone, BooleanSupplier consumeFreshDrone) {
        if (remainingUses > 0) return new UseResult(true, false, remainingUses - 1);
        if (!consumeFreshDrone.getAsBoolean()) return new UseResult(false, false, 0);
        return new UseResult(true, true, usesPerDrone - 1);
    }

    public static int firstMissingProblem(byte maintenanceProblems) {
        for (int problem = 0; problem < 6; problem++) {
            if ((maintenanceProblems & 1 << problem) == 0) return problem;
        }
        return -1;
    }

    public static boolean isBetterStation(double candidateDistance, int candidateX, int candidateY, int candidateZ,
                                          double currentDistance, int currentX, int currentY, int currentZ) {
        if (Double.compare(candidateDistance, currentDistance) != 0) return candidateDistance < currentDistance;
        if (candidateX != currentX) return candidateX < currentX;
        if (candidateY != currentY) return candidateY < currentY;
        return candidateZ < currentZ;
    }

    public static ControlAction sleepAction(boolean active) {
        return active ? ControlAction.SUSPEND_AFTER_FINISH : ControlAction.DISABLE_NOW;
    }

    public static boolean providesCleanroom(@Nullable DroneTier tier, boolean online) {
        return online && (tier == DroneTier.SANGUINE || tier == DroneTier.PLASMATIC);
    }

    public record UseResult(boolean successful, boolean consumedFreshDrone, int remainingUses) {}

    public enum ControlAction {
        SUSPEND_AFTER_FINISH,
        DISABLE_NOW
    }
}
