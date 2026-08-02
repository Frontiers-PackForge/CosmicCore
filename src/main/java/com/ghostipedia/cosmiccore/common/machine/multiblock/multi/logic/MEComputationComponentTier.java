package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.gregtechceu.gtceu.api.GTValues;

public enum MEComputationComponentTier {

    LV(GTValues.LV, 24, 128, 24),
    MV(GTValues.MV, 48, 256, 96),
    HV(GTValues.HV, 96, 512, 384),
    EV(GTValues.EV, 192, 1024, 1536),
    IV(GTValues.IV, 384, 2048, 6144),
    LUV(GTValues.LuV, 768, 4096, 24576);

    private final int gtTier;
    private final long coreEuPerTick;
    private final long coreCwutPerTick;
    private final long relayEuPerTick;

    MEComputationComponentTier(int gtTier, long coreEuPerTick, long coreCwutPerTick, long relayEuPerTick) {
        if (Math.multiplyExact(coreEuPerTick, MEComputationArrayTuning.CORE_EU_RATIO_DENOMINATOR) !=
                Math.multiplyExact(coreCwutPerTick, MEComputationArrayTuning.CORE_EU_RATIO_NUMERATOR)) {
            throw new IllegalArgumentException("ME computation component tier violates the shared EU to CWU ratio");
        }
        this.gtTier = gtTier;
        this.coreEuPerTick = coreEuPerTick;
        this.coreCwutPerTick = coreCwutPerTick;
        this.relayEuPerTick = relayEuPerTick;
    }

    public int gtTier() {
        return gtTier;
    }

    public long coreEuPerTick() {
        return coreEuPerTick;
    }

    public long coreCwutPerTick() {
        return coreCwutPerTick;
    }

    public long coreStandbyEuPerTick() {
        return Math.max(1, Math.ceilDiv(coreEuPerTick, 8));
    }

    public long relayEuPerTick() {
        return relayEuPerTick;
    }
}
