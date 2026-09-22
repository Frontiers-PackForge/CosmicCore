package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm;

import java.util.function.IntPredicate;

public final class BloomwyrmAllocationPolicy {

    private BloomwyrmAllocationPolicy() {}

    public static int highestPassingParallel(int maximum, IntPredicate canRun) {
        int lower = 0;
        int upper = Math.max(0, maximum);
        while (lower < upper) {
            int candidate = lower + (upper - lower + 1) / 2;
            if (canRun.test(candidate)) {
                lower = candidate;
            } else {
                upper = candidate - 1;
            }
        }
        return lower;
    }

    public static boolean shouldCloseBatch(boolean batchActive, int activeUnits, boolean pendingAllocations) {
        return batchActive && activeUnits == 0 && !pendingAllocations;
    }
}
