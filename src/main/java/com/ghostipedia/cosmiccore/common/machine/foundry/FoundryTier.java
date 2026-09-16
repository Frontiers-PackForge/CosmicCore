package com.ghostipedia.cosmiccore.common.machine.foundry;

public enum FoundryTier {

    TIER_1(1, 4),
    TIER_2(2, 8),
    TIER_3(3, 16);

    private final int level;
    private final int capacity;

    FoundryTier(int level, int capacity) {
        this.level = level;
        this.capacity = capacity;
    }

    public int level() {
        return level;
    }

    public int capacity() {
        return capacity;
    }

    public static FoundryTier fromLevel(int level) {
        for (FoundryTier tier : values()) {
            if (tier.level == level) return tier;
        }
        return TIER_1;
    }
}
