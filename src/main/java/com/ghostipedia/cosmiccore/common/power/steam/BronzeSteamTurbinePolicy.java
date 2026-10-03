package com.ghostipedia.cosmiccore.common.power.steam;

import com.gregtechceu.gtceu.api.GTValues;

public final class BronzeSteamTurbinePolicy {

    public static final long BASE_OUTPUT = GTValues.V[GTValues.LV] * 4;

    private BronzeSteamTurbinePolicy() {}

    public static double holderPowerMultiplier(int holderTier) {
        return Math.pow(1.5, Math.max(0, holderTier - GTValues.LV));
    }

    public static double holderEfficiencyMultiplier(int holderTier) {
        return Math.pow(1.1, Math.max(0, holderTier - GTValues.LV));
    }

    public static double combinedEfficiency(int holderTier, int rotorEfficiency) {
        return holderEfficiencyMultiplier(holderTier) * rotorEfficiency / 100.0;
    }

    public static long mechanicalOutput(int holderTier, int rotorPower) {
        return Math.max(0L, (long) Math.floor(BASE_OUTPUT * holderPowerMultiplier(holderTier) * rotorPower / 100.0));
    }

    public static long ratedOutput(int holderTier, int rotorPower, long dynamoCapacity) {
        return Math.min(mechanicalOutput(holderTier, rotorPower), Math.max(0L, dynamoCapacity));
    }

    public static int requiredParallels(long ratedOutput, long recipeOutput) {
        if (ratedOutput <= 0 || recipeOutput <= 0) return 0;
        return Math.toIntExact(Math.min(Integer.MAX_VALUE, (ratedOutput + recipeOutput - 1) / recipeOutput));
    }

    public static double outputMultiplier(long ratedOutput, long recipeOutput, int requiredParallels,
                                          int actualParallels) {
        if (ratedOutput <= 0 || recipeOutput <= 0 || actualParallels <= 0) return 0;
        return actualParallels == requiredParallels ? (double) ratedOutput / recipeOutput : actualParallels;
    }

    public static double durationMultiplier(double efficiency, long ratedOutput, long recipeOutput,
                                            int requiredParallels, int actualParallels) {
        if (efficiency <= 0 || ratedOutput <= 0 || recipeOutput <= 0 || actualParallels <= 0) return 0;
        if (actualParallels != requiredParallels) return efficiency;
        return efficiency * actualParallels * recipeOutput / ratedOutput;
    }
}
