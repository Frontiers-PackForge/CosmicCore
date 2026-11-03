package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.compat.FeCompat;

import appeng.api.config.PowerUnit;

public final class MEComputationArrayTuning {

    public static final int MINIMUM_ENERGY_HATCH_TIER = GTValues.MV;
    public static final int COMPONENT_POSITIONS = 5;
    public static final int LARGE_COMPONENT_POSITIONS = 9;
    public static final int COOLANT_MB_PER_MINUTE = 4000;
    public static final int TICKS_PER_MINUTE = 1200;
    public static final int COOLED_CWU_MULTIPLIER = 4;
    public static final int UPLINK_BUFFER_TICKS = 20;
    public static final long CORE_EU_RATIO_NUMERATOR = 3;
    public static final long CORE_EU_RATIO_DENOMINATOR = 16;
    public static final MEComputationComponentTier LOW_POWER_MINIMUM_COMPONENT_TIER = MEComputationComponentTier.LV;
    public static final MEComputationComponentTier LOW_POWER_MAXIMUM_COMPONENT_TIER = MEComputationComponentTier.HV;
    public static final MEComputationComponentTier LARGE_MAXIMUM_COMPONENT_TIER = MEComputationComponentTier.LUV;

    private MEComputationArrayTuning() {}

    public static long coreEuForCwut(long cwut, boolean cooled) {
        return Math.ceilDiv(Math.multiplyExact(Math.max(0, cwut), cooled ? 9L : CORE_EU_RATIO_NUMERATOR),
                cooled ? 128L : CORE_EU_RATIO_DENOMINATOR);
    }

    public static long cwutForCoreEu(long eu, boolean cooled) {
        return Math.multiplyExact(Math.max(0, eu), cooled ? 128L : CORE_EU_RATIO_DENOMINATOR) /
                (cooled ? 9L : CORE_EU_RATIO_NUMERATOR);
    }

    public static long cooledStandbyEu(long standbyEu) {
        return Math.ceilDiv(Math.multiplyExact(standbyEu, 3), 2);
    }

    public static int coolantForTick(int remainder) {
        return (Math.floorMod(remainder, TICKS_PER_MINUTE) + COOLANT_MB_PER_MINUTE) / TICKS_PER_MINUTE;
    }

    public static int nextCoolantRemainder(int remainder) {
        return (Math.floorMod(remainder, TICKS_PER_MINUTE) + COOLANT_MB_PER_MINUTE) % TICKS_PER_MINUTE;
    }

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

    public static double uplinkBufferCapacityAe(long relayEuPerTick) {
        return euToAe(relayEuPerTick * UPLINK_BUFFER_TICKS);
    }
}
