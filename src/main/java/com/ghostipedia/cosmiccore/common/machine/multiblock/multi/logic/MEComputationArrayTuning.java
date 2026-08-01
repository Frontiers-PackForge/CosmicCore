package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.compat.FeCompat;

import appeng.api.config.PowerUnit;

public final class MEComputationArrayTuning {

    public static final int MINIMUM_ENERGY_HATCH_TIER = GTValues.MV;
    public static final int COMPONENT_POSITIONS = 5;
    public static final long CORE_EU_PER_TICK = 24;
    public static final long CORE_CWU_PER_TICK = 128;
    public static final long RELAY_EU_PER_TICK = 24;
    public static final int UPLINK_BUFFER_TICKS = 20;

    private MEComputationArrayTuning() {}

    public static double euToAe(long eu) {
        long fe = FeCompat.toFeLong(eu, FeCompat.ratio(false));
        return PowerUnit.FE.convertTo(PowerUnit.AE, fe);
    }

    public static double aeToEu(double ae) {
        double fe = PowerUnit.AE.convertTo(PowerUnit.FE, ae);
        return Math.max(0, fe / FeCompat.ratio(false));
    }

    public static long aeToEuFloor(double ae) {
        return (long) Math.floor(aeToEu(ae));
    }

    public static double uplinkBufferCapacityAe() {
        long relayEuPerTick = RELAY_EU_PER_TICK * COMPONENT_POSITIONS;
        return euToAe(relayEuPerTick * UPLINK_BUFFER_TICKS);
    }
}
