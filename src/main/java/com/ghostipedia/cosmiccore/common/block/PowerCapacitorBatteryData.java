package com.ghostipedia.cosmiccore.common.block;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.multiblock.IBatteryData;

import org.jetbrains.annotations.NotNull;

public enum PowerCapacitorBatteryData implements IBatteryData {

    EMPTY_TIER_0(-1, 0, "empty_tier_0_capacitor"),
    LV_CAPACITOR(GTValues.LV, 5_000_000, "lv_capacitor_battery"),
    MV_CAPACITOR(GTValues.MV, 25_000_000, "mv_capacitor_battery"),
    HV_CAPACITOR(GTValues.HV, 75_000_000, "hv_capacitor_battery");

    private final int tier;
    private final long capacity;
    private final String batteryName;

    PowerCapacitorBatteryData(int tier, long capacity, String batteryName) {
        this.tier = tier;
        this.capacity = capacity;
        this.batteryName = batteryName;
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    @Override
    public @NotNull String getBatteryName() {
        return batteryName;
    }
}
