package com.ghostipedia.cosmiccore.common.rate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.Arrays;

final class RateCalculatorSelection {

    static final String FIRST = "first";
    static final String SECOND = "second";
    static final String DIRECT = "direct";
    static final String REGIONS = "regions";

    private RateCalculatorSelection() {}

    static void clear(CompoundTag tag) {
        for (String key : new String[] { FIRST, SECOND, DIRECT, REGIONS, "dimension", "report", "savedAt" })
            tag.remove(key);
    }

    static void migrate(CompoundTag tag) {
        if (tag.contains(DIRECT) || tag.contains(REGIONS) || (!tag.contains(FIRST) && !tag.contains(SECOND))) return;
        if (tag.contains(SECOND)) {
            ListTag regions = new ListTag();
            CompoundTag region = new CompoundTag();
            region.putLong(FIRST, tag.getLong(FIRST));
            region.putLong(SECOND, tag.getLong(SECOND));
            regions.add(region);
            tag.put(REGIONS, regions);
            tag.remove(FIRST);
            tag.remove(SECOND);
        }
        tag.putLongArray(DIRECT, new long[0]);
    }

    static boolean addDirect(CompoundTag tag, long position, int maximum) {
        long[] direct = tag.getLongArray(DIRECT);
        for (long selected : direct) if (selected == position) return true;
        if (direct.length >= maximum) return false;
        long[] expanded = Arrays.copyOf(direct, direct.length + 1);
        expanded[direct.length] = position;
        tag.putLongArray(DIRECT, expanded);
        return true;
    }

    static boolean completeRegion(CompoundTag tag, long second, int maximum) {
        ListTag regions = tag.getList(REGIONS, Tag.TAG_COMPOUND).copy();
        if (regions.size() >= maximum) return false;
        CompoundTag region = new CompoundTag();
        region.putLong(FIRST, tag.getLong(FIRST));
        region.putLong(SECOND, second);
        regions.add(region);
        tag.put(REGIONS, regions);
        tag.remove(FIRST);
        return true;
    }
}
