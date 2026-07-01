package com.ghostipedia.cosmiccore.common.food;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

import java.util.List;

public record FoodDefinition(FoodCategory category, double heartBonus, double regenBonus, int durationTicks,
                             List<EffectSpec> effects) {

    public record EffectSpec(Holder<MobEffect> effect, int amplifier) {}
}
