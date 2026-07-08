package com.ghostipedia.cosmiccore.common.food;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

import java.util.List;

public record FoodDefinition(FoodCategory category, double heartBonus, double regenBonus, int durationTicks,
                             List<EffectSpec> effects, List<AttributeSpec> attributes, List<BehaviorLine> behaviors,
                             List<ConsumeEffectSpec> consumeEffects) {

    private static final double HEALTH_PER_HEART = 2.0;

    public static double healthFromHearts(double hearts) {
        return hearts * HEALTH_PER_HEART;
    }

    public static double heartsFromHealth(double health) {
        return health / HEALTH_PER_HEART;
    }

    public record EffectSpec(Holder<MobEffect> effect, int amplifier) {}

    public record ConsumeEffectSpec(Holder<MobEffect> effect, int amplifier, int durationTicks) {}
}
