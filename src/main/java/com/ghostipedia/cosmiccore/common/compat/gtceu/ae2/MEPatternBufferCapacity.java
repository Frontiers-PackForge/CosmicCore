package com.ghostipedia.cosmiccore.common.compat.gtceu.ae2;

import java.util.function.IntFunction;
import java.util.function.Supplier;

public final class MEPatternBufferCapacity {

    public static final int COLUMNS = 9;
    public static final int ROWS = 4;
    public static final int SLOTS = COLUMNS * ROWS;

    private MEPatternBufferCapacity() {}

    public static <T> T[] expandSavedSlots(T[] current, IntFunction<T[]> arrayFactory, Supplier<T> slotFactory) {
        if (current.length >= SLOTS) return current;
        T[] expanded = arrayFactory.apply(SLOTS);
        System.arraycopy(current, 0, expanded, 0, current.length);
        for (int index = current.length; index < expanded.length; index++) {
            expanded[index] = slotFactory.get();
        }
        return expanded;
    }
}
