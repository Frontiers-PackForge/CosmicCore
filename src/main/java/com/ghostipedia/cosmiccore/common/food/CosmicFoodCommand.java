package com.ghostipedia.cosmiccore.common.food;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.mojang.brigadier.CommandDispatcher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class CosmicFoodCommand {

    private CosmicFoodCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cosmicfood")
                .then(Commands.literal("dump")
                        .requires(source -> source.hasPermission(2))
                        .executes(ctx -> dump(ctx.getSource())))
                .then(Commands.literal("memory")
                        .requires(source -> source.hasPermission(2))
                        .executes(ctx -> testMemory(ctx.getSource())))
                .then(Commands.literal("inscribe").executes(ctx -> inscribe(ctx.getSource()))));
    }

    private static int inscribe(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable("cosmiccore.command.players_only"));
            return 0;
        }
        Component result = HearthLogic.inscribeCurrentMeal(player);
        source.sendSuccess(() -> result, false);
        return 1;
    }

    private static int testMemory(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable("cosmiccore.command.players_only"));
            return 0;
        }
        ItemStack held = player.getMainHandItem();
        if (!HearthLogic.applyHomeMeal(player, held, 0, 0)) {
            source.sendFailure(Component.translatable("cosmiccore.command.food.memory_fail"));
            return 0;
        }
        held.shrink(1);
        return 1;
    }

    private static int dump(CommandSourceStack source) {
        List<Item> foods = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (CosmicFoodRegistry.isConsumable(new ItemStack(item))) {
                foods.add(item);
            }
        }
        foods.sort(Comparator.comparing(item -> BuiltInRegistries.ITEM.getKey(item).toString()));

        StringBuilder csv = new StringBuilder();
        csv.append("id,name,category,archetype,defined,nutrition,saturation,hearts,regen_per_s,duration_s,")
                .append("vanilla_effects,defined_effects,food_tags\n");
        StringBuilder js = new StringBuilder();
        int defined = 0;

        for (Item item : foods) {
            ItemStack stack = new ItemStack(item);
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            boolean isDefined = CosmicFoodRegistry.isDefined(item);
            if (isDefined) defined++;
            FoodDefinition def = CosmicFoodRegistry.get(stack);
            FoodProperties props = stack.get(DataComponents.FOOD);
            int nutrition = props != null ? props.nutrition() : 0;
            float saturation = props != null ? props.saturation() : 0f;

            StringBuilder vanillaEffects = new StringBuilder();
            if (props != null) {
                for (FoodProperties.PossibleEffect possible : props.effects()) {
                    MobEffectInstance instance = possible.effect();
                    if (!vanillaEffects.isEmpty()) vanillaEffects.append("; ");
                    vanillaEffects.append(effectId(instance)).append(" amp").append(instance.getAmplifier())
                            .append(" ").append(instance.getDuration()).append("t")
                            .append(" p").append(number(possible.probability()));
                }
            }

            StringBuilder definedEffects = new StringBuilder();
            for (FoodDefinition.EffectSpec spec : def.effects()) {
                if (!definedEffects.isEmpty()) definedEffects.append("; ");
                definedEffects.append(spec.effect().unwrapKey().map(k -> k.location().toString()).orElse("?"))
                        .append(" amp").append(spec.amplifier());
            }

            csv.append(id).append(",\"").append(stack.getHoverName().getString().replace("\"", "'")).append("\",")
                    .append(def.category().name().toLowerCase(Locale.ROOT)).append(",")
                    .append(CosmicFoodRegistry.archetypeNameFor(stack)).append(",")
                    .append(isDefined).append(",")
                    .append(nutrition).append(",").append(number(saturation)).append(",")
                    .append(number(FoodDefinition.heartsFromHealth(def.heartBonus()))).append(",")
                    .append(number(def.regenBonus())).append(",")
                    .append(def.durationTicks() / 20).append(",")
                    .append("\"").append(vanillaEffects).append("\",")
                    .append("\"").append(definedEffects).append("\",")
                    .append("\"").append(foodTags(stack)).append("\"\n");

            if (!isDefined) {
                js.append("CosmicFood.define('").append(id).append("', food => {\n");
                if (def.category() == FoodCategory.BREW) {
                    js.append("    food.category('brew')\n");
                }
                js.append("    food.health(").append(number(FoodDefinition.heartsFromHealth(def.heartBonus())))
                        .append(")\n");
                js.append("    food.regen(").append(number(def.regenBonus())).append(")\n");
                js.append("    food.duration('").append(duration(def.durationTicks())).append("')\n");
                if (props != null) {
                    for (FoodProperties.PossibleEffect possible : props.effects()) {
                        MobEffectInstance instance = possible.effect();
                        js.append("    food.effect('").append(effectId(instance)).append("', ")
                                .append(instance.getAmplifier()).append(")\n");
                    }
                }
                js.append("})\n");
            }
        }

        Path dir = source.getServer().getServerDirectory();
        Path csvPath = dir.resolve("cosmiccore_food_dump.csv");
        Path jsPath = dir.resolve("cosmiccore_food_dump.js");
        try {
            Files.writeString(csvPath, csv.toString());
            Files.writeString(jsPath, js.toString());
        } catch (IOException e) {
            source.sendFailure(Component.translatable("cosmiccore.command.food.dump_fail", e.getMessage()));
            return 0;
        }

        int total = foods.size();
        int definedCount = defined;
        source.sendSuccess(() -> Component.translatable("cosmiccore.command.food.dump_done", total, definedCount,
                csvPath.getFileName().toString(), jsPath.getFileName().toString()), false);
        return total;
    }

    private static String foodTags(ItemStack stack) {
        StringBuilder tags = new StringBuilder();
        stack.getItemHolder().tags().forEach(tag -> {
            String path = tag.location().getPath();
            if (path.startsWith("foods") || path.contains("food") || path.startsWith("crops")) {
                if (!tags.isEmpty()) tags.append("; ");
                tags.append(tag.location());
            }
        });
        return tags.toString();
    }

    private static String effectId(MobEffectInstance instance) {
        return instance.getEffect().unwrapKey().map(key -> key.location().toString()).orElse("?");
    }

    private static String number(double value) {
        if (value == Math.floor(value)) return String.valueOf((long) value);
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static String duration(int ticks) {
        int seconds = ticks / 20;
        if (seconds % 60 == 0) return (seconds / 60) + "m";
        return seconds + "s";
    }
}
