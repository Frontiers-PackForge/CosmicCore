package com.ghostipedia.cosmiccore.common.compat.apothic;

public final class ApothicEffectDamage {

    private ApothicEffectDamage() {}

    public static boolean needsRepair(float resistanceDamage, float damage, int sunderingAmplifier) {
        if (!Float.isFinite(resistanceDamage) || !Float.isFinite(damage)) return true;
        if (sunderingAmplifier < 0) return false;
        return !Float.isFinite(resistanceDamage + damage * (sunderingAmplifier + 1) * 0.2f);
    }

    public static float repair(float damage, int resistanceAmplifier, int sunderingAmplifier) {
        double finiteDamage = Float.isNaN(damage) || damage <= 0 ? 0 : Math.min((double) damage, Float.MAX_VALUE);
        long resistanceLevel = Math.max(0L, (long) resistanceAmplifier + 1);
        long sunderingLevel = Math.max(0L, (long) sunderingAmplifier + 1);
        double result = finiteDamage * ((25.0 - resistanceLevel * 5.0 + sunderingLevel * 5.0) / 25.0);
        return (float) Math.clamp(result, 0.0, (double) Float.MAX_VALUE);
    }
}
