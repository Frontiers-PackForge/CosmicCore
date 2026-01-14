package com.ghostipedia.cosmiccore.integration.emi.favorites;

import net.minecraft.resources.ResourceLocation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CosmicRecipeBookmark {

    private final EmiStack output;
    private final List<EmiIngredient> inputs;
    @Nullable
    private final ResourceLocation recipeId;
    private long multiplier = 1;

    public CosmicRecipeBookmark(EmiStack output, List<EmiIngredient> inputs, @Nullable ResourceLocation recipeId) {
        this.output = output;
        this.inputs = new ArrayList<>(inputs);
        this.recipeId = recipeId;
    }

    public static CosmicRecipeBookmark fromRecipe(EmiRecipe recipe) {
        if (recipe == null || recipe.getOutputs().isEmpty()) {
            return null;
        }

        EmiStack output = recipe.getOutputs().get(0);
        List<EmiIngredient> inputs = new ArrayList<>(recipe.getInputs());

        return new CosmicRecipeBookmark(output, inputs, recipe.getId());
    }

    public EmiStack getOutput() {
        return output;
    }

    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Nullable
    public ResourceLocation getRecipeId() {
        return recipeId;
    }

    @Nullable
    public EmiRecipe getRecipe() {
        if (recipeId == null) {
            return null;
        }
        return EmiApi.getRecipeManager().getRecipe(recipeId);
    }

    public long getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(long multiplier) {
        this.multiplier = Math.max(1, multiplier);
    }

    public void adjustMultiplier(long delta) {
        this.multiplier = Math.max(1, this.multiplier + delta);
    }

    public long getOutputAmount() {
        return output.getAmount() * multiplier;
    }

    public long getInputAmount(int index) {
        if (index < 0 || index >= inputs.size()) {
            return 0;
        }
        return inputs.get(index).getAmount() * multiplier;
    }

    public int getTotalItemCount() {
        return 1 + inputs.size();
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();

        JsonElement outputJson = EmiIngredientSerializer.getSerialized(output);
        if (outputJson != null) {
            obj.add("output", outputJson);
        }

        JsonArray inputsArr = new JsonArray();
        for (EmiIngredient input : inputs) {
            JsonElement inputJson = EmiIngredientSerializer.getSerialized(input);
            if (inputJson != null) {
                inputsArr.add(inputJson);
            }
        }
        obj.add("inputs", inputsArr);

        if (recipeId != null) {
            obj.addProperty("recipeId", recipeId.toString());
        }

        if (multiplier != 1) {
            obj.addProperty("multiplier", multiplier);
        }

        return obj;
    }

    public static CosmicRecipeBookmark fromJson(JsonObject obj) {
        if (!obj.has("output")) {
            return null;
        }

        EmiIngredient outputIngredient = EmiIngredientSerializer.getDeserialized(obj.get("output"));
        if (outputIngredient.isEmpty() || outputIngredient.getEmiStacks().isEmpty()) {
            return null;
        }
        EmiStack output = outputIngredient.getEmiStacks().get(0);

        List<EmiIngredient> inputs = new ArrayList<>();
        if (obj.has("inputs")) {
            JsonArray inputsArr = obj.getAsJsonArray("inputs");
            for (JsonElement el : inputsArr) {
                EmiIngredient input = EmiIngredientSerializer.getDeserialized(el);
                if (!input.isEmpty()) {
                    inputs.add(input);
                }
            }
        }

        ResourceLocation recipeId = null;
        if (obj.has("recipeId")) {
            recipeId = new ResourceLocation(obj.get("recipeId").getAsString());
        }

        CosmicRecipeBookmark bookmark = new CosmicRecipeBookmark(output, inputs, recipeId);

        if (obj.has("multiplier")) {
            bookmark.setMultiplier(obj.get("multiplier").getAsLong());
        }

        return bookmark;
    }
}
