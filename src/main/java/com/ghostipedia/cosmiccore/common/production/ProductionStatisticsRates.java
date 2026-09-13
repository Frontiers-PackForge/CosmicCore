package com.ghostipedia.cosmiccore.common.production;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ProductionStatisticsRates {

    private ProductionStatisticsRates() {}

    public static BigDecimal perTick(BigDecimal amount, long ticks) {
        return ticks <= 0 ? BigDecimal.ZERO : amount.divide(BigDecimal.valueOf(ticks), 6, RoundingMode.HALF_UP);
    }
}
