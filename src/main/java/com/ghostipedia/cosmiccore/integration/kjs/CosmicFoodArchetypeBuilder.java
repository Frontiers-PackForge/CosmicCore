package com.ghostipedia.cosmiccore.integration.kjs;

import com.ghostipedia.cosmiccore.common.food.FoodArchetype;
import com.ghostipedia.cosmiccore.common.food.FoodCategory;

public class CosmicFoodArchetypeBuilder {

    private final String name;
    private FoodCategory category = FoodCategory.FOOD;
    private int nutritionMin = 0;
    private int nutritionMax = 20;
    private double heartsMin = 2;
    private double heartsMax = 4;
    private double regenMin = 0.25;
    private double regenMax = 0.5;
    private int durationMinTicks = 6000;
    private int durationMaxTicks = 12000;

    public CosmicFoodArchetypeBuilder(String name) {
        this.name = name;
    }

    public CosmicFoodArchetypeBuilder brew() {
        this.category = FoodCategory.BREW;
        return this;
    }

    public CosmicFoodArchetypeBuilder nutrition(int min, int max) {
        this.nutritionMin = min;
        this.nutritionMax = max;
        return this;
    }

    public CosmicFoodArchetypeBuilder hearts(double min, double max) {
        this.heartsMin = min;
        this.heartsMax = max;
        return this;
    }

    public CosmicFoodArchetypeBuilder regen(double min, double max) {
        this.regenMin = min;
        this.regenMax = max;
        return this;
    }

    public CosmicFoodArchetypeBuilder duration(Object min, Object max) {
        this.durationMinTicks = CosmicFoodBuilder.parseDuration(min);
        this.durationMaxTicks = CosmicFoodBuilder.parseDuration(max);
        return this;
    }

    public FoodArchetype build() {
        return new FoodArchetype(name, category, nutritionMin, nutritionMax, heartsMin, heartsMax,
                regenMin, regenMax, durationMinTicks, durationMaxTicks);
    }
}
