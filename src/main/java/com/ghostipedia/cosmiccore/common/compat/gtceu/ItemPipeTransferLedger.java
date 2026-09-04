package com.ghostipedia.cosmiccore.common.compat.gtceu;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.util.function.ToIntFunction;

public final class ItemPipeTransferLedger {

    private ItemPipeTransferLedger() {}

    public static <K> int decayAndCompact(Object2IntMap<K> ledger, ToIntFunction<K> decayAmount) {
        long total = 0;
        var iterator = ledger.object2IntEntrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            int remaining = entry.getIntValue() - Math.max(0, decayAmount.applyAsInt(entry.getKey()));
            if (remaining <= 0) {
                iterator.remove();
            } else {
                entry.setValue(remaining);
                total = Math.min(Integer.MAX_VALUE, total + remaining);
            }
        }
        return (int) total;
    }

    public static void subtractAndCompact(Object2IntMap<?> ledger, int amount) {
        var iterator = ledger.object2IntEntrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            int remaining = entry.getIntValue() - Math.max(0, amount);
            if (remaining <= 0) {
                iterator.remove();
            } else {
                entry.setValue(remaining);
            }
        }
    }

    public static int saturatedAdd(int left, int right) {
        long result = (long) left + right;
        return (int) Math.clamp(result, 0L, Integer.MAX_VALUE);
    }
}
