package com.ghostipedia.cosmiccore.common.food;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class CosmicFoodRegistry {

    private CosmicFoodRegistry() {}

    public static final int MIN_DURATION = 6000;
    public static final int MAX_DURATION = 72000;
    public static final float ABSORB_PROBABILITY_THRESHOLD = 0.5f;
    public static final int ABSORB_MIN_DURATION_TICKS = 200;

    private static final ResourceKey<MobEffect> FD_NOURISHMENT = ResourceKey.create(
            Registries.MOB_EFFECT, ResourceLocation.fromNamespaceAndPath("farmersdelight", "nourishment"));

    private static final Map<Item, FoodDefinition> DEFS = new ConcurrentHashMap<>();
    private static final Set<Item> EXPLICIT = ConcurrentHashMap.newKeySet();
    private static final Map<Item, FoodTailor> TAILORS = new ConcurrentHashMap<>();
    private static final Set<Item> EXCLUDED = ConcurrentHashMap.newKeySet();
    private static final Set<Item> VILE = ConcurrentHashMap.newKeySet();

    public static void register(Item item, FoodDefinition def) {
        if (TAILORS.remove(item) != null) {
            CosmicCore.LOGGER.warn("CosmicFood: define replaces an earlier tailor for {}",
                    BuiltInRegistries.ITEM.getKey(item));
        }
        DEFS.put(item, def);
        EXPLICIT.add(item);
    }

    public static void tailor(Item item, FoodTailor tailor) {
        if (EXPLICIT.contains(item)) {
            CosmicCore.LOGGER.warn("CosmicFood: ignoring tailor for {}, an explicit define is author truth",
                    BuiltInRegistries.ITEM.getKey(item));
            return;
        }
        TAILORS.put(item, tailor);
        DEFS.remove(item);
    }

    public static boolean isDefined(Item item) {
        return EXPLICIT.contains(item);
    }

    public static void exclude(Item item) {
        EXCLUDED.add(item);
        DEFS.remove(item);
    }

    public static void vile(Item item) {
        VILE.add(item);
    }

    public static boolean isVile(Item item) {
        return VILE.contains(item);
    }

    public static void clearResolved() {
        DEFS.keySet().removeIf(item -> !EXPLICIT.contains(item));
    }

    public static boolean isConsumable(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() == Items.ROTTEN_FLESH) return false;
        if (EXCLUDED.contains(stack.getItem())) return false;
        if (DEFS.containsKey(stack.getItem())) return true;
        return stack.has(DataComponents.FOOD) || stack.getUseAnimation() == UseAnim.DRINK;
    }

    public static FoodDefinition get(ItemStack stack) {
        FoodDefinition def = DEFS.get(stack.getItem());
        if (def != null) return def;
        def = resolve(stack);
        DEFS.put(stack.getItem(), def);
        return def;
    }

    public enum PlateRole {

        MAIN("cosmiccore.food.role.main"),
        SIDE("cosmiccore.food.role.side"),
        DRINK("cosmiccore.food.role.drink");

        public final String key;

        PlateRole(String key) {
            this.key = key;
        }

        public Component label() {
            return Component.translatable(key);
        }
    }

    public static PlateRole plateRole(ItemStack stack) {
        return plateRole(stack, archetypeNameFor(stack));
    }

    public static PlateRole plateRole(ItemStack stack, String archetypeName) {
        if (get(stack).category() == FoodCategory.BREW) return PlateRole.DRINK;
        if ("treat".equals(archetypeName)) return PlateRole.SIDE;
        return PlateRole.MAIN;
    }

    public static String archetypeNameFor(ItemStack stack) {
        if (isDefined(stack.getItem())) return "defined";
        StackProfile profile = StackProfile.of(stack);
        return profile.archetype() != null ? profile.archetype().name() : "auto";
    }

    private record StackProfile(FoodCategory category, int nutrition, float saturation,
                                @Nullable FoodProperties food, @Nullable FoodArchetype archetype) {

        static StackProfile of(ItemStack stack) {
            boolean drink = stack.getUseAnimation() == UseAnim.DRINK;
            FoodCategory category = drink ? FoodCategory.BREW : FoodCategory.FOOD;
            FoodProperties food = stack.get(DataComponents.FOOD);
            int nutrition = food != null ? food.nutrition() : (drink ? 4 : 2);
            float saturation = food != null ? food.saturation() : (drink ? 4f : 2f);
            FoodArchetype archetype = FoodArchetypes.resolve(
                    BuiltInRegistries.ITEM.getKey(stack.getItem()), category, nutrition);
            return new StackProfile(category, nutrition, saturation, food, archetype);
        }
    }

    private static FoodDefinition resolve(ItemStack stack) {
        StackProfile profile = StackProfile.of(stack);
        List<FoodDefinition.EffectSpec> absorbed = new ArrayList<>();
        List<FoodDefinition.ConsumeEffectSpec> oneShot = new ArrayList<>();
        splitEffects(profile.food(), absorbed, oneShot);

        FoodDefinition base = profile.archetype() != null ?
                profile.archetype().resolve(profile.nutrition(), List.copyOf(absorbed), List.copyOf(oneShot)) :
                autoDefault(profile.category(), profile.nutrition(), profile.saturation(), List.copyOf(absorbed),
                        List.copyOf(oneShot));

        FoodTailor tailor = TAILORS.get(stack.getItem());
        return tailor != null ? tailor.apply(base) : base;
    }

    public static boolean isAbsorbed(FoodProperties.PossibleEffect possible) {
        if (possible.probability() < ABSORB_PROBABILITY_THRESHOLD) return false;
        if (possible.effect().getDuration() < ABSORB_MIN_DURATION_TICKS) return false;
        var holder = possible.effect().getEffect();
        if (isBlockedEffect(holder)) return false;
        return holder.value().getCategory() == MobEffectCategory.BENEFICIAL;
    }

    public static boolean isBlockedEffect(Holder<MobEffect> holder) {
        return holder.is(FD_NOURISHMENT);
    }

    private static void splitEffects(FoodProperties food, List<FoodDefinition.EffectSpec> absorbed,
                                     List<FoodDefinition.ConsumeEffectSpec> oneShot) {
        if (food == null || food.effects().isEmpty()) return;
        for (FoodProperties.PossibleEffect possible : food.effects()) {
            var instance = possible.effect();
            if (isBlockedEffect(instance.getEffect())) continue;
            if (isAbsorbed(possible)) {
                absorbed.add(new FoodDefinition.EffectSpec(instance.getEffect(), instance.getAmplifier()));
            } else {
                oneShot.add(new FoodDefinition.ConsumeEffectSpec(instance.getEffect(), instance.getAmplifier(),
                        instance.getDuration()));
            }
        }
    }

    private static FoodDefinition autoDefault(FoodCategory category, int nutrition, float saturation,
                                              List<FoodDefinition.EffectSpec> absorbed,
                                              List<FoodDefinition.ConsumeEffectSpec> oneShot) {
        double hearts = Math.max(nutrition, 2);
        double regen = Mth.clamp(nutrition * 0.10, 0.25, 2.0);
        int duration = Mth.clamp((int) ((nutrition + saturation) * 600), MIN_DURATION, MAX_DURATION);
        return new FoodDefinition(category, hearts, regen, duration, absorbed, List.of(), List.of(), oneShot);
    }
}
