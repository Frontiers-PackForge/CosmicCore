package com.ghostipedia.cosmiccore.common.commands;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.sammy.malum.core.systems.spirit.EntitySpiritDropData;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class VitaeCultivationExportCommand {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int DEFAULT_SAMPLES = 4_096;

    private VitaeCultivationExportCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cosmicvitae")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("export")
                        .executes(context -> export(context.getSource(), DEFAULT_SAMPLES))
                        .then(Commands.argument("samples", IntegerArgumentType.integer(256, 65_536))
                                .executes(context -> export(
                                        context.getSource(),
                                        IntegerArgumentType.getInteger(context, "samples"))))));
    }

    private static int export(CommandSourceStack source, int samples) {
        ServerLevel level = source.getLevel();
        var killer = FakePlayerFactory.getMinecraft(level);
        List<JsonObject> entities = new ArrayList<>();
        List<EntityType<?>> types = BuiltInRegistries.ENTITY_TYPE.stream()
                .sorted(Comparator.comparing(type -> BuiltInRegistries.ENTITY_TYPE.getKey(type).toString()))
                .toList();

        for (EntityType<?> type : types) {
            Entity entity;
            try {
                entity = type.create(level);
            } catch (RuntimeException exception) {
                entities.add(failedEntity(type, exception));
                continue;
            }
            if (!(entity instanceof LivingEntity living)) {
                if (entity != null) entity.discard();
                continue;
            }
            living.setPos(source.getPosition());
            try {
                entities.add(exportEntity(level, killer, living, samples));
            } catch (RuntimeException exception) {
                entities.add(failedEntity(type, exception));
            } finally {
                living.discard();
            }
        }

        JsonObject root = new JsonObject();
        root.addProperty("schema", "COSMIC_VITAE_CULTIVATION_SNAPSHOT_V1");
        root.addProperty("generatedAt", Instant.now().toString());
        root.addProperty("samplesPerEntity", samples);
        JsonArray entityArray = new JsonArray();
        entities.forEach(entityArray::add);
        root.add("entities", entityArray);
        root.addProperty("fingerprint", fingerprint(entityArray));

        Path directory = source.getServer().getServerDirectory().resolve("cosmiccore");
        Path output = directory.resolve("vitae-cultivation-snapshot.json");
        try {
            Files.createDirectories(directory);
            Files.writeString(output, GSON.toJson(root));
        } catch (IOException exception) {
            source.sendFailure(Component.translatable(
                    "cosmiccore.command.vitae.export_fail",
                    exception.getMessage()));
            return 0;
        }

        int resolved = (int) entities.stream().filter(entity -> !entity.has("error")).count();
        source.sendSuccess(() -> Component.translatable(
                "cosmiccore.command.vitae.export_done",
                resolved,
                output.getFileName()), false);
        return resolved;
    }

    private static JsonObject exportEntity(ServerLevel level, ServerPlayer killer, LivingEntity living, int samples) {
        EntityType<?> type = living.getType();
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, living)
                .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, killer)
                .withParameter(LootContextParams.DAMAGE_SOURCE, killer.damageSources().playerAttack(killer))
                .withParameter(LootContextParams.ATTACKING_ENTITY, killer)
                .withParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, killer)
                .withParameter(LootContextParams.ORIGIN, living.position())
                .withLuck(0.0F)
                .create(LootContextParamSets.ENTITY);
        LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(living.getLootTable());
        Map<ResourceLocation, DropAccumulator> drops = sampleDrops(lootTable, params, samples);

        JsonObject result = new JsonObject();
        result.addProperty("entity", id.toString());
        result.addProperty("name", living.getName().getString());
        result.addProperty("namespace", id.getNamespace());
        result.addProperty("category", type.getCategory().getName());
        result.addProperty("lootTable", living.getLootTable().location().toString());
        result.addProperty("canSummon", type.canSummon());
        result.addProperty("enemy", living instanceof Enemy);
        result.addProperty("boss", living instanceof EnderDragon || living instanceof WitherBoss);
        result.addProperty("npc", living instanceof AbstractVillager);
        result.addProperty("width", type.getDimensions().width());
        result.addProperty("height", type.getDimensions().height());
        result.add("experience", sampleExperience(level, killer, living));

        JsonArray dropArray = new JsonArray();
        drops.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            DropAccumulator drop = entry.getValue();
            JsonObject object = new JsonObject();
            object.addProperty("item", entry.getKey().toString());
            object.addProperty("name", BuiltInRegistries.ITEM.get(entry.getKey()).getDescription().getString());
            object.addProperty("min", drop.minimum);
            object.addProperty("max", drop.maximum);
            object.addProperty("observedChance", (double) drop.hits / samples);
            object.addProperty("hits", drop.hits);
            dropArray.add(object);
        });
        result.add("loot", dropArray);
        result.add("malum", exportMalum(living));
        return result;
    }

    private static Map<ResourceLocation, DropAccumulator> sampleDrops(
                                                                      LootTable lootTable,
                                                                      LootParams params,
                                                                      int samples) {
        Map<ResourceLocation, DropAccumulator> totals = new LinkedHashMap<>();
        for (int sample = 0; sample < samples; sample++) {
            Map<ResourceLocation, Integer> sampleCounts = new LinkedHashMap<>();
            for (ItemStack stack : lootTable.getRandomItems(params, RandomSource.create(sample))) {
                if (stack.isEmpty()) continue;
                ResourceLocation item = BuiltInRegistries.ITEM.getKey(stack.getItem());
                sampleCounts.merge(item, stack.getCount(), Integer::sum);
            }
            for (var entry : totals.entrySet()) {
                entry.getValue().observe(sampleCounts.getOrDefault(entry.getKey(), 0));
            }
            for (var entry : sampleCounts.entrySet()) {
                if (totals.containsKey(entry.getKey())) continue;
                DropAccumulator accumulator = new DropAccumulator(sample);
                accumulator.observe(entry.getValue());
                totals.put(entry.getKey(), accumulator);
            }
        }
        return totals;
    }

    private static JsonObject sampleExperience(ServerLevel level, ServerPlayer killer, LivingEntity living) {
        int minimum = Integer.MAX_VALUE;
        int maximum = Integer.MIN_VALUE;
        for (int sample = 0; sample < 256; sample++) {
            int value = living.getExperienceReward(level, killer);
            minimum = Math.min(minimum, value);
            maximum = Math.max(maximum, value);
        }
        JsonObject result = new JsonObject();
        result.addProperty("min", minimum == Integer.MAX_VALUE ? 0 : minimum);
        result.addProperty("max", maximum == Integer.MIN_VALUE ? 0 : maximum);
        return result;
    }

    private static JsonObject exportMalum(LivingEntity living) {
        JsonObject result = new JsonObject();
        var data = EntitySpiritDropData.getSpiritData(living);
        result.addProperty("resolved", data.isPresent());
        if (data.isEmpty()) return result;
        result.addProperty("primary", data.get().getPrimaryType().getRegistryName().toString());
        JsonArray outputs = new JsonArray();
        for (ItemStack stack : data.get().getSpiritStacks()) {
            JsonObject output = new JsonObject();
            output.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            output.addProperty("count", stack.getCount());
            outputs.add(output);
        }
        result.add("outputs", outputs);
        return result;
    }

    private static JsonObject failedEntity(EntityType<?> type, RuntimeException exception) {
        JsonObject result = new JsonObject();
        result.addProperty("entity", BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
        result.addProperty("error", exception.getClass().getSimpleName() + ": " + exception.getMessage());
        return result;
    }

    private static String fingerprint(JsonArray entities) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(GSON.toJson(entities).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static final class DropAccumulator {

        private int minimum;
        private int maximum;
        private int hits;

        private DropAccumulator(int priorSamples) {
            minimum = priorSamples == 0 ? Integer.MAX_VALUE : 0;
            maximum = 0;
            hits = 0;
        }

        private void observe(int count) {
            minimum = Math.min(minimum, count);
            maximum = Math.max(maximum, count);
            if (count > 0) hits++;
        }
    }
}
