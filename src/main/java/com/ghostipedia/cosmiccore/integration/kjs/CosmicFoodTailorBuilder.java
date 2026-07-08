package com.ghostipedia.cosmiccore.integration.kjs;

import com.ghostipedia.cosmiccore.common.food.AttributeSpec;
import com.ghostipedia.cosmiccore.common.food.BehaviorLine;
import com.ghostipedia.cosmiccore.common.food.FoodCategory;
import com.ghostipedia.cosmiccore.common.food.FoodDefinition;
import com.ghostipedia.cosmiccore.common.food.FoodTailor;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CosmicFoodTailorBuilder {

    @Nullable
    private Double heartBonus;
    @Nullable
    private Double regenBonus;
    @Nullable
    private Integer durationTicks;
    @Nullable
    private FoodCategory category;
    private final List<FoodDefinition.EffectSpec> effects = new ArrayList<>();
    private final List<AttributeSpec> attributes = new ArrayList<>();
    private final List<BehaviorLine> behaviors = new ArrayList<>();

    public CosmicFoodTailorBuilder category(String category) {
        this.category = FoodCategory.valueOf(category.toUpperCase());
        return this;
    }

    public CosmicFoodTailorBuilder health(double hearts) {
        this.heartBonus = FoodDefinition.healthFromHearts(hearts);
        return this;
    }

    public CosmicFoodTailorBuilder regen(double perSecond) {
        this.regenBonus = perSecond;
        return this;
    }

    public CosmicFoodTailorBuilder duration(Object duration) {
        this.durationTicks = CosmicFoodBuilder.parseDuration(duration);
        return this;
    }

    public CosmicFoodTailorBuilder effect(String id, int amplifier) {
        effects.add(CosmicFoodBuilder.effectSpec(id, amplifier));
        return this;
    }

    public CosmicFoodTailorBuilder attribute(String id, double amount) {
        return attribute(id, amount, "add");
    }

    public CosmicFoodTailorBuilder attribute(String id, double amount, String operation) {
        attributes.add(CosmicFoodBuilder.attributeSpec(id, amount, operation));
        return this;
    }

    public CosmicFoodTailorBuilder behavior(String glyph, String color, String label, String value) {
        behaviors.add(new BehaviorLine(glyph, CosmicFoodBuilder.parseColor(color), label, value));
        return this;
    }

    public FoodTailor build() {
        return new FoodTailor(heartBonus, regenBonus, durationTicks, category, List.copyOf(effects),
                List.copyOf(attributes), List.copyOf(behaviors));
    }
}
