package com.ghostipedia.cosmiccore.common.compat.lso;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import org.jetbrains.annotations.Nullable;
import sfiomn.legendarysurvivaloverhaul.api.data.json.JsonTemperatureConsumable;
import sfiomn.legendarysurvivaloverhaul.api.data.manager.TemperatureDataManager;
import sfiomn.legendarysurvivaloverhaul.registry.AttributeRegistry;
import sfiomn.legendarysurvivaloverhaul.registry.MobEffectRegistry;

public final class LsoFoodCompat {

    private LsoFoodCompat() {}

    public static final String MOD_ID = "legendarysurvivaloverhaul";
    public static final double DEGREES_PER_LEVEL = 3.0;

    @Nullable
    private static Boolean loaded;

    public static boolean isLoaded() {
        if (loaded == null) loaded = ModList.get().isLoaded(MOD_ID);
        return loaded;
    }

    public record ConsumableTemp(int level, int durationTicks) {}

    public static void retuneEffects() {
        MobEffectRegistry.HOT_FOOD.get().addAttributeModifier(AttributeRegistry.HEATING_TEMPERATURE,
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "hot_food_modifier"),
                DEGREES_PER_LEVEL, AttributeModifier.Operation.ADD_VALUE);
        MobEffectRegistry.HOT_DRINk.get().addAttributeModifier(AttributeRegistry.HEATING_TEMPERATURE,
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "hot_drink_modifier"),
                DEGREES_PER_LEVEL, AttributeModifier.Operation.ADD_VALUE);
        MobEffectRegistry.COLD_FOOD.get().addAttributeModifier(AttributeRegistry.COOLING_TEMPERATURE,
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "cold_food_modifier"),
                -DEGREES_PER_LEVEL, AttributeModifier.Operation.ADD_VALUE);
        MobEffectRegistry.COLD_DRINK.get().addAttributeModifier(AttributeRegistry.COOLING_TEMPERATURE,
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "cold_drink_modifier"),
                -DEGREES_PER_LEVEL, AttributeModifier.Operation.ADD_VALUE);
    }

    @Nullable
    public static ConsumableTemp temperature(ItemStack stack) {
        if (!isLoaded() || TemperatureDataManager.internalConsumable == null) return null;
        var entries = TemperatureDataManager.getConsumable(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        if (entries == null || entries.isEmpty()) return null;
        for (JsonTemperatureConsumable entry : entries) {
            if (entry.temperatureLevel != 0) {
                return new ConsumableTemp(entry.temperatureLevel, entry.duration);
            }
        }
        return null;
    }
}
