package com.ghostipedia.cosmiccore.client.rate;

import net.minecraft.nbt.CompoundTag;

import java.util.Comparator;
import java.util.List;

final class RateCalculatorRowOrdering {

    static final String DEFAULT_MODE = "default";

    private RateCalculatorRowOrdering() {}

    static List<CompoundTag> sorted(List<CompoundTag> rows, String mode, boolean reverse) {
        Comparator<CompoundTag> comparator = switch (normalize(mode)) {
            case "id" -> Comparator.comparing(RateCalculatorSimulation::key);
            case "produced" -> Comparator.comparingDouble(
                    (CompoundTag row) -> rate(row, "observedOut")).reversed()
                    .thenComparing(RateCalculatorSimulation::key);
            case "consumed" -> Comparator.comparingDouble(
                    (CompoundTag row) -> rate(row, "observedIn")).reversed()
                    .thenComparing(RateCalculatorSimulation::key);
            default -> Comparator.comparingDouble(RateCalculatorRowOrdering::activity).reversed()
                    .thenComparing(RateCalculatorSimulation::key);
        };
        return rows.stream().sorted(reverse ? comparator.reversed() : comparator).toList();
    }

    static String normalize(String mode) {
        return switch (mode) {
            case "default", "id", "produced", "consumed" -> mode;
            default -> DEFAULT_MODE;
        };
    }

    private static double activity(CompoundTag row) {
        return rate(row, "observedIn") + rate(row, "observedOut");
    }

    private static double rate(CompoundTag row, String key) {
        double value = row.getDouble(key);
        return Double.isFinite(value) ? Math.max(0, value) : 0;
    }
}
