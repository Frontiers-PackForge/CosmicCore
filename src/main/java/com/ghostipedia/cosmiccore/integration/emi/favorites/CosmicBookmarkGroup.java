package com.ghostipedia.cosmiccore.integration.emi.favorites;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CosmicBookmarkGroup {

    public enum GroupType {
        REGULAR,
        RECIPE
    }

    public sealed interface BookmarkEntry permits StackEntry, RecipeEntry {

        String id();
    }

    public record IngredientSnapshot(JsonElement ingredient, long amount) {

        public IngredientSnapshot {
            ingredient = ingredient.deepCopy();
            amount = Math.max(1, amount);
        }

        @Override
        public JsonElement ingredient() {
            return ingredient.deepCopy();
        }
    }

    public static final class StackEntry implements BookmarkEntry {

        private final String id;
        private final JsonElement ingredient;
        private final @Nullable Long amountOverride;
        private final @Nullable String recipeId;

        public StackEntry(String id, JsonElement ingredient, @Nullable Long amountOverride,
                          @Nullable String recipeId) {
            this.id = id;
            this.ingredient = ingredient.deepCopy();
            this.amountOverride = amountOverride == null ? null : Math.max(1, amountOverride);
            this.recipeId = amountOverride == null ? recipeId : null;
        }

        public StackEntry(JsonElement ingredient, @Nullable Long amountOverride, @Nullable String recipeId) {
            this(UUID.randomUUID().toString(), ingredient, amountOverride, recipeId);
        }

        @Override
        public String id() {
            return id;
        }

        public JsonElement ingredient() {
            return ingredient.deepCopy();
        }

        public @Nullable Long amountOverride() {
            return amountOverride;
        }

        public @Nullable String recipeId() {
            return recipeId;
        }
    }

    public static final class RecipeEntry implements BookmarkEntry {

        private final String id;
        private final @Nullable String recipeId;
        private final IngredientSnapshot selectedOutput;
        private final List<IngredientSnapshot> inputs;
        private final List<IngredientSnapshot> catalysts;
        private final List<IngredientSnapshot> outputs;
        private long batches;

        public RecipeEntry(String id, @Nullable String recipeId, IngredientSnapshot selectedOutput,
                           List<IngredientSnapshot> inputs, List<IngredientSnapshot> catalysts,
                           List<IngredientSnapshot> outputs, long batches) {
            this.id = id;
            this.recipeId = recipeId;
            this.selectedOutput = selectedOutput;
            this.inputs = List.copyOf(inputs);
            this.catalysts = List.copyOf(catalysts);
            this.outputs = List.copyOf(outputs);
            this.batches = Math.max(1, batches);
        }

        public RecipeEntry(@Nullable String recipeId, IngredientSnapshot selectedOutput,
                           List<IngredientSnapshot> inputs, List<IngredientSnapshot> catalysts,
                           List<IngredientSnapshot> outputs) {
            this(UUID.randomUUID().toString(), recipeId, selectedOutput, inputs, catalysts, outputs, 1);
        }

        @Override
        public String id() {
            return id;
        }

        public @Nullable String recipeId() {
            return recipeId;
        }

        public IngredientSnapshot selectedOutput() {
            return selectedOutput;
        }

        public List<IngredientSnapshot> inputs() {
            return inputs;
        }

        public List<IngredientSnapshot> catalysts() {
            return catalysts;
        }

        public List<IngredientSnapshot> outputs() {
            return outputs;
        }

        public long batches() {
            return batches;
        }

        void adjustBatches(long delta) {
            batches = Math.max(1, saturatingAdd(batches, delta));
        }

        private static long saturatingAdd(long value, long delta) {
            if (delta > 0 && value > Long.MAX_VALUE - delta) return Long.MAX_VALUE;
            if (delta < 0 && value < Long.MIN_VALUE - delta) return Long.MIN_VALUE;
            return value + delta;
        }
    }

    private final String id;
    private String name;
    private GroupType type;
    private final List<BookmarkEntry> entries;

    public CosmicBookmarkGroup(String name) {
        this(name, GroupType.REGULAR);
    }

    public CosmicBookmarkGroup(String name, GroupType type) {
        this(UUID.randomUUID().toString(), name, type, List.of());
    }

    public CosmicBookmarkGroup(String id, String name, GroupType type, List<BookmarkEntry> entries) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.entries = new ArrayList<>(entries);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    public GroupType getType() {
        return type;
    }

    void setType(GroupType type) {
        this.type = type;
    }

    public boolean isRecipeGroup() {
        return type == GroupType.RECIPE;
    }

    List<BookmarkEntry> getEntries() {
        return entries;
    }

    void replaceEntries(List<BookmarkEntry> replacements) {
        entries.clear();
        entries.addAll(replacements);
    }

    public int size() {
        return entries.size();
    }
}
