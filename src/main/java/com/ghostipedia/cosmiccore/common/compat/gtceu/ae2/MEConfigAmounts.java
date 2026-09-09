package com.ghostipedia.cosmiccore.common.compat.gtceu.ae2;

public final class MEConfigAmounts {

    private MEConfigAmounts() {}

    public static int scroll(long current, double delta, boolean control, int maximum) {
        long bounded = Math.clamp(current, 1, maximum);
        if (delta == 0 || !Double.isFinite(delta)) return (int) bounded;
        long next = control ? (delta > 0 ? bounded * 2 : bounded / 2) :
                bounded + (delta > 0 ? 1 : -1);
        return (int) Math.clamp(next, 1, maximum);
    }
}
