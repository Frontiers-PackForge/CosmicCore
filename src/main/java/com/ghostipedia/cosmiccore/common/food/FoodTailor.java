package com.ghostipedia.cosmiccore.common.food;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record FoodTailor(@Nullable Double heartBonus, @Nullable Double regenBonus, @Nullable Integer durationTicks,
                         @Nullable FoodCategory category, List<FoodDefinition.EffectSpec> effects,
                         List<AttributeSpec> attributes, List<BehaviorLine> behaviors) {

    public FoodDefinition apply(FoodDefinition base) {
        List<FoodDefinition.EffectSpec> mergedEffects = new ArrayList<>(base.effects());
        mergedEffects.addAll(effects);
        List<AttributeSpec> mergedAttributes = new ArrayList<>(base.attributes());
        mergedAttributes.addAll(attributes);
        List<BehaviorLine> mergedBehaviors = new ArrayList<>(base.behaviors());
        mergedBehaviors.addAll(behaviors);
        return new FoodDefinition(
                category != null ? category : base.category(),
                heartBonus != null ? heartBonus : base.heartBonus(),
                regenBonus != null ? regenBonus : base.regenBonus(),
                durationTicks != null ? durationTicks : base.durationTicks(),
                List.copyOf(mergedEffects), List.copyOf(mergedAttributes), List.copyOf(mergedBehaviors),
                base.consumeEffects());
    }
}
