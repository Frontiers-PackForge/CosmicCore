package com.ghostipedia.cosmiccore.common.compat.gtceu.transformer;

import com.gregtechceu.gtceu.api.GTValues;

public final class TransformerStepDownPolicy {

    public static final int DEFAULT_TRANSITIONS = 1;
    public static final int MAX_TRANSITIONS = 3;
    public static final int AUTOMATIC_AMPERAGE = 0;

    private TransformerStepDownPolicy() {}

    public static int maxTransitions(int inputTier) {
        return Math.max(DEFAULT_TRANSITIONS, Math.min(MAX_TRANSITIONS, inputTier));
    }

    public static int transitions(int inputTier, int requestedTransitions) {
        return Math.max(DEFAULT_TRANSITIONS, Math.min(maxTransitions(inputTier), requestedTransitions));
    }

    public static int outputTier(int inputTier, int requestedTransitions) {
        return inputTier - transitions(inputTier, requestedTransitions);
    }

    public static int maximumOutputAmperage(int baseAmperage, int inputTier, int requestedTransitions) {
        if (baseAmperage <= 0) return 1;
        long inputVoltage = GTValues.V[inputTier];
        long outputVoltage = GTValues.V[outputTier(inputTier, requestedTransitions)];
        long ratio = Math.max(1L, inputVoltage / outputVoltage);
        long maximum = saturatingMultiply(baseAmperage, ratio);
        return maximum > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) maximum;
    }

    public static int effectiveOutputAmperage(int storedAmperage, int maximumAmperage) {
        if (storedAmperage == AUTOMATIC_AMPERAGE) return maximumAmperage;
        return Math.max(1, Math.min(maximumAmperage, storedAmperage));
    }

    public static int storedOutputAmperage(int requestedAmperage, int maximumAmperage) {
        if (requestedAmperage <= AUTOMATIC_AMPERAGE) return AUTOMATIC_AMPERAGE;
        return Math.max(1, Math.min(maximumAmperage, requestedAmperage));
    }

    public static long storageCapacity(long inputVoltage, int inputAmperage) {
        return saturatingMultiply(saturatingMultiply(inputVoltage, inputAmperage), 8L);
    }

    public static int overlayAmperage(int amperage) {
        if (amperage >= 64) return 64;
        if (amperage >= 16) return 16;
        if (amperage >= 8) return 8;
        if (amperage >= 4) return 4;
        if (amperage >= 2) return 2;
        return 1;
    }

    private static long saturatingMultiply(long left, long right) {
        try {
            return Math.multiplyExact(left, right);
        } catch (ArithmeticException ignored) {
            return Long.MAX_VALUE;
        }
    }
}
