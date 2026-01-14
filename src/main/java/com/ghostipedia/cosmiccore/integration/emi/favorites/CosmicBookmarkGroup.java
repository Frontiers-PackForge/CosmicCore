package com.ghostipedia.cosmiccore.integration.emi.favorites;

import net.minecraft.util.GsonHelper;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import dev.emi.emi.runtime.EmiFavorite;

import java.util.List;

public class CosmicBookmarkGroup {

    public enum ViewMode {
        DEFAULT,
        TODO_LIST
    }

    private String name;
    private final List<EmiFavorite> favorites;
    private final List<CosmicRecipeBookmark> recipeBookmarks;
    private ViewMode viewMode = ViewMode.DEFAULT;

    public CosmicBookmarkGroup(String name) {
        this.name = name;
        this.favorites = Lists.newArrayList();
        this.recipeBookmarks = Lists.newArrayList();
    }

    public ViewMode getViewMode() {
        return viewMode;
    }

    public void setViewMode(ViewMode viewMode) {
        this.viewMode = viewMode;
    }

    public void toggleViewMode() {
        this.viewMode = (viewMode == ViewMode.DEFAULT) ? ViewMode.TODO_LIST : ViewMode.DEFAULT;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<EmiFavorite> getFavorites() {
        return favorites;
    }

    public void addFavorite(EmiFavorite favorite) {
        for (int i = 0; i < favorites.size(); i++) {
            EmiFavorite existing = favorites.get(i);
            if (existing instanceof CosmicFavorite cf && favorite instanceof CosmicFavorite newCf) {
                if (cf.strictEquals(newCf) && cf.getRecipe() == newCf.getRecipe()) {
                    favorites.remove(i);
                    return;
                }
            } else if (existing.strictEquals(favorite) && existing.getRecipe() == EmiApi.getRecipeContext(favorite)) {
                favorites.remove(i);
                return;
            }
        }
        favorites.add(favorite);
    }

    public void addFavoriteAt(EmiFavorite favorite, int index) {
        for (int i = 0; i < favorites.size(); i++) {
            if (favorites.get(i).strictEquals(favorite)) {
                if (i < index) {
                    index--;
                }
                favorites.remove(i);
                break;
            }
        }

        if (index < 0) index = 0;
        if (index >= favorites.size()) {
            favorites.add(favorite);
        } else {
            favorites.add(index, favorite);
        }
    }

    public boolean removeFavorite(EmiIngredient stack) {
        for (int i = 0; i < favorites.size(); i++) {
            if (favorites.get(i).strictEquals(stack)) {
                favorites.remove(i);
                return true;
            }
        }
        return false;
    }

    public void clear() {
        favorites.clear();
        recipeBookmarks.clear();
    }

    public List<CosmicRecipeBookmark> getRecipeBookmarks() {
        return recipeBookmarks;
    }

    public void addRecipeBookmark(CosmicRecipeBookmark bookmark) {
        if (bookmark == null) return;

        if (bookmark.getRecipeId() != null) {
            for (int i = 0; i < recipeBookmarks.size(); i++) {
                CosmicRecipeBookmark existing = recipeBookmarks.get(i);
                if (bookmark.getRecipeId().equals(existing.getRecipeId())) {
                    recipeBookmarks.remove(i);
                    return;
                }
            }
        }

        recipeBookmarks.add(bookmark);
    }

    public boolean removeRecipeBookmark(int index) {
        if (index >= 0 && index < recipeBookmarks.size()) {
            recipeBookmarks.remove(index);
            return true;
        }
        return false;
    }

    public CosmicRecipeBookmark getRecipeBookmark(int index) {
        if (index >= 0 && index < recipeBookmarks.size()) {
            return recipeBookmarks.get(index);
        }
        return null;
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", name);
        obj.addProperty("viewMode", viewMode.name());

        JsonArray arr = new JsonArray();
        for (EmiFavorite fav : favorites) {
            JsonElement stack = EmiIngredientSerializer.getSerialized(fav.getStack());
            if (stack != null) {
                JsonObject favObj = new JsonObject();
                favObj.add("stack", stack);

                if (fav.getRecipe() != null && fav.getRecipe().getId() != null) {
                    favObj.addProperty("recipe", fav.getRecipe().getId().toString());
                }

                if (fav instanceof CosmicFavorite cf && cf.hasCustomAmount()) {
                    favObj.addProperty("amount", cf.getCustomAmount());
                }

                arr.add(favObj);
            }
        }
        obj.add("favorites", arr);

        JsonArray recipeArr = new JsonArray();
        for (CosmicRecipeBookmark bookmark : recipeBookmarks) {
            recipeArr.add(bookmark.toJson());
        }
        obj.add("recipeBookmarks", recipeArr);

        return obj;
    }

    public static CosmicBookmarkGroup fromJson(JsonObject obj) {
        String name = GsonHelper.getAsString(obj, "name", "Unnamed");
        CosmicBookmarkGroup group = new CosmicBookmarkGroup(name);

        if (GsonHelper.isValidNode(obj, "viewMode")) {
            try {
                group.viewMode = ViewMode.valueOf(GsonHelper.getAsString(obj, "viewMode"));
            } catch (IllegalArgumentException ignored) {}
        }

        JsonArray arr = GsonHelper.getAsJsonArray(obj, "favorites", new JsonArray());
        for (JsonElement el : arr) {
            if (el.isJsonObject()) {
                JsonObject favObj = el.getAsJsonObject();

                EmiRecipe recipe = null;
                if (GsonHelper.isValidNode(favObj, "recipe")) {
                    recipe = EmiApi.getRecipeManager().getRecipe(EmiPort.id(GsonHelper.getAsString(favObj, "recipe")));
                }

                if (GsonHelper.isValidNode(favObj, "stack")) {
                    EmiIngredient ingredient = EmiIngredientSerializer.getDeserialized(favObj.get("stack"));
                    if (ingredient.isEmpty()) {
                        continue;
                    }
                    if (ingredient instanceof EmiStack es) {
                        ingredient = es.copy();
                    }

                    if (GsonHelper.isValidNode(favObj, "amount")) {
                        long amount = GsonHelper.getAsLong(favObj, "amount");
                        group.favorites.add(new CosmicFavorite(ingredient, recipe, amount));
                    } else {
                        group.favorites.add(new EmiFavorite(ingredient, recipe));
                    }
                }
            }
        }

        JsonArray recipeArr = GsonHelper.getAsJsonArray(obj, "recipeBookmarks", new JsonArray());
        for (JsonElement el : recipeArr) {
            if (el.isJsonObject()) {
                CosmicRecipeBookmark bookmark = CosmicRecipeBookmark.fromJson(el.getAsJsonObject());
                if (bookmark != null) {
                    group.recipeBookmarks.add(bookmark);
                }
            }
        }

        return group;
    }
}
