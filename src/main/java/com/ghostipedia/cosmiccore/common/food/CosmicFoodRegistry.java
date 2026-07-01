package com.ghostipedia.cosmiccore.common.food;

import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CosmicFoodRegistry {

    private CosmicFoodRegistry() {}

    public static final int MIN_DURATION = 6000;
    public static final int MAX_DURATION = 72000;

    private static final Map<Item, FoodDefinition> DEFS = new ConcurrentHashMap<>();

    public static void register(Item item, FoodDefinition def) {
        DEFS.put(item, def);
    }

    public static boolean isConsumable(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() == Items.ROTTEN_FLESH) return false;
        if (DEFS.containsKey(stack.getItem())) return true;
        return stack.has(DataComponents.FOOD) || stack.getUseAnimation() == UseAnim.DRINK;
    }

    public static FoodDefinition get(ItemStack stack) {
        FoodDefinition def = DEFS.get(stack.getItem());
        if (def != null) return def;
        def = autoDefault(stack);
        DEFS.put(stack.getItem(), def);
        return def;
    }

    private static FoodDefinition autoDefault(ItemStack stack) {
        boolean drink = stack.getUseAnimation() == UseAnim.DRINK;
        FoodCategory category = drink ? FoodCategory.BREW : FoodCategory.FOOD;

        FoodProperties food = stack.get(DataComponents.FOOD);
        int nutrition = food != null ? food.nutrition() : (drink ? 4 : 2);
        float saturation = food != null ? food.saturation() : (drink ? 4f : 2f);

        double hearts = Math.max(nutrition, 2);
        double regen = Mth.clamp(nutrition * 0.10, 0.25, 2.0);
        int duration = Mth.clamp((int) ((nutrition + saturation) * 600), MIN_DURATION, MAX_DURATION);

        return new FoodDefinition(category, hearts, regen, duration, List.of());
    }
}
