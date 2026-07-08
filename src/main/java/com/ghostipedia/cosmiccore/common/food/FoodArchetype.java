package com.ghostipedia.cosmiccore.common.food;

import net.minecraft.util.Mth;

import java.util.List;

public record FoodArchetype(String name, FoodCategory category, int nutritionMin, int nutritionMax,
                            double heartsMin, double heartsMax, double regenMin, double regenMax,
                            int durationMinTicks, int durationMaxTicks) {

    public FoodDefinition resolve(int nutrition, List<FoodDefinition.EffectSpec> effects,
                                  List<FoodDefinition.ConsumeEffectSpec> consumeEffects) {
        float t = nutritionMax > nutritionMin ?
                Mth.clamp((nutrition - nutritionMin) / (float) (nutritionMax - nutritionMin), 0f, 1f) : 0f;
        double hearts = Mth.lerp(t, heartsMin, heartsMax);
        double regen = Mth.lerp(t, regenMin, regenMax);
        int duration = (int) Mth.lerp(t, durationMinTicks, durationMaxTicks);
        return new FoodDefinition(category, FoodDefinition.healthFromHearts(hearts), regen, duration, effects,
                List.of(), List.of(), consumeEffects);
    }

    public int distance(int nutrition) {
        if (nutrition < nutritionMin) return nutritionMin - nutrition;
        if (nutrition > nutritionMax) return nutrition - nutritionMax;
        return 0;
    }
}
