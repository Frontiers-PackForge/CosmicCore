package com.ghostipedia.cosmiccore.common.power;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.WireProperties;
import com.gregtechceu.gtceu.utils.GTUtil;

public final class ConductorAmpacityRules {

    private static final int BASE_TIER_CAP = 32;

    private ConductorAmpacityRules() {}

    public static int effectiveAmperage(WireProperties properties, boolean insulated) {
        if (!insulated && !properties.isSuperconductor()) {
            return properties.getAmperage();
        }
        long doubledAmperage = Math.min((long) properties.getAmperage() * 2L, Integer.MAX_VALUE);
        return (int) Math.min(doubledAmperage,
                maximumAmperageForTier(GTUtil.getTierByVoltage(properties.getVoltage())));
    }

    public static int maximumAmperageForTier(int tier) {
        if (tier <= 0) {
            return BASE_TIER_CAP;
        }
        if (tier >= 26) {
            return Integer.MAX_VALUE;
        }
        return BASE_TIER_CAP << tier;
    }
}
