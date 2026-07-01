package com.ghostipedia.cosmiccore.integration.kjs;

import com.ghostipedia.cosmiccore.common.food.AttributeSpec;
import com.ghostipedia.cosmiccore.common.food.BehaviorLine;
import com.ghostipedia.cosmiccore.common.food.FoodCategory;
import com.ghostipedia.cosmiccore.common.food.FoodDefinition;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public class CosmicFoodBuilder {

    private final Item item;
    private FoodCategory category = FoodCategory.FOOD;
    private double heartBonus = 0;
    private double regenBonus = 0;
    private int durationTicks = 6000;
    private final List<FoodDefinition.EffectSpec> effects = new ArrayList<>();
    private final List<AttributeSpec> attributes = new ArrayList<>();
    private final List<BehaviorLine> behaviors = new ArrayList<>();

    public CosmicFoodBuilder(Item item) {
        this.item = item;
    }

    public CosmicFoodBuilder category(String category) {
        this.category = FoodCategory.valueOf(category.toUpperCase());
        return this;
    }

    public CosmicFoodBuilder health(double hearts) {
        this.heartBonus = hearts * 2.0;
        return this;
    }

    public CosmicFoodBuilder regen(double perSecond) {
        this.regenBonus = perSecond;
        return this;
    }

    public CosmicFoodBuilder duration(Object duration) {
        this.durationTicks = parseDuration(duration);
        return this;
    }

    public CosmicFoodBuilder effect(String id, int amplifier) {
        effects.add(new FoodDefinition.EffectSpec(effectHolder(id), amplifier));
        return this;
    }

    public CosmicFoodBuilder attribute(String id, double amount) {
        return attribute(id, amount, "add");
    }

    public CosmicFoodBuilder attribute(String id, double amount, String operation) {
        attributes.add(new AttributeSpec(attributeHolder(id), amount, operation(operation)));
        return this;
    }

    public CosmicFoodBuilder behavior(String glyph, String color, String label, String value) {
        behaviors.add(new BehaviorLine(glyph, parseColor(color), label, value));
        return this;
    }

    public FoodDefinition build() {
        return new FoodDefinition(category, heartBonus, regenBonus, durationTicks, List.copyOf(effects),
                List.copyOf(attributes), List.copyOf(behaviors));
    }

    private static Holder<MobEffect> effectHolder(String id) {
        return BuiltInRegistries.MOB_EFFECT
                .getHolder(ResourceKey.create(Registries.MOB_EFFECT, ResourceLocation.parse(id)))
                .orElseThrow(() -> new IllegalArgumentException("CosmicFood: unknown effect " + id));
    }

    private static Holder<Attribute> attributeHolder(String id) {
        return BuiltInRegistries.ATTRIBUTE
                .getHolder(ResourceKey.create(Registries.ATTRIBUTE, ResourceLocation.parse(id)))
                .orElseThrow(() -> new IllegalArgumentException("CosmicFood: unknown attribute " + id));
    }

    private static AttributeModifier.Operation operation(String operation) {
        return switch (operation.toLowerCase()) {
            case "percent", "total_percent" -> AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
            case "base_percent" -> AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
            default -> AttributeModifier.Operation.ADD_VALUE;
        };
    }

    private static int parseColor(String color) {
        String hex = color.startsWith("#") ? color.substring(1) : color;
        return 0xFF000000 | Integer.parseInt(hex, 16);
    }

    private static int parseDuration(Object duration) {
        if (duration instanceof Number number) return number.intValue();
        String s = duration.toString().trim().toLowerCase();
        if (s.isEmpty()) return 6000;
        char unit = s.charAt(s.length() - 1);
        if (Character.isDigit(unit)) return Integer.parseInt(s);
        int n = Integer.parseInt(s.substring(0, s.length() - 1).trim());
        return switch (unit) {
            case 's' -> n * 20;
            case 'm' -> n * 1200;
            case 'h' -> n * 72000;
            default -> n;
        };
    }
}
