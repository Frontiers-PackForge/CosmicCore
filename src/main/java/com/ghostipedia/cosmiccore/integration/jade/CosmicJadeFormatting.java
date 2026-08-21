package com.ghostipedia.cosmiccore.integration.jade;

import java.util.Locale;

public final class CosmicJadeFormatting {

    private CosmicJadeFormatting() {}

    public static String fixedTwoDecimals(double value) {
        return String.format(Locale.ROOT, "%,.2f", Double.isFinite(value) ? value : 0.0D);
    }
}
