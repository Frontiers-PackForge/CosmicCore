package com.ghostipedia.cosmiccore.common.food;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

import java.util.List;

public record FoodDefinition(FoodCategory category, double heartBonus, double regenBonus, int durationTicks,
                             List<EffectSpec> effects, List<AttributeSpec> attributes, List<BehaviorLine> behaviors) {

    public record EffectSpec(Holder<MobEffect> effect, int amplifier) {}
}
