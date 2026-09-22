package com.ghostipedia.cosmiccore.common.transmission.energy;

public final class PowerTowerEnergyPolicy {

    public static final int MAX_HATCHES_PER_DIRECTION = 16;

    private PowerTowerEnergyPolicy() {}

    public static boolean acceptsHatchCount(int count) {
        return count >= 0 && count <= MAX_HATCHES_PER_DIRECTION;
    }
}
