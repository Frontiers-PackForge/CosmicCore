package com.ghostipedia.cosmiccore.integration.emi.favorites;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.client.Minecraft;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.runtime.EmiFavorite;
import dev.emi.emi.runtime.EmiFavorites;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

public class CosmicBookmarkManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILENAME = "cosmic_bookmarks.json";

    private static CosmicBookmarkManager instance;

    private final List<CosmicBookmarkGroup> groups;
    private int activeGroupIndex;

    private CosmicBookmarkManager() {
        this.groups = Lists.newArrayList();
        this.activeGroupIndex = 0;
        groups.add(new CosmicBookmarkGroup("Default"));
    }

    public static CosmicBookmarkManager getInstance() {
        if (instance == null) {
            instance = new CosmicBookmarkManager();
            instance.load();
        }
        return instance;
    }

    public CosmicBookmarkGroup getActiveGroup() {
        if (activeGroupIndex >= groups.size()) {
            activeGroupIndex = 0;
        }
        return groups.get(activeGroupIndex);
    }

    public List<CosmicBookmarkGroup> getGroups() {
        return groups;
    }

    public int getActiveGroupIndex() {
        return activeGroupIndex;
    }

    public int getGroupCount() {
        return groups.size();
    }

    public void setActiveGroup(int index) {
        if (index >= 0 && index < groups.size()) {
            activeGroupIndex = index;
            syncToEmi();
            save();
        }
    }

    public void nextGroup() {
        activeGroupIndex = (activeGroupIndex + 1) % groups.size();
        syncToEmi();
        save();
    }

    public void previousGroup() {
        activeGroupIndex = (activeGroupIndex - 1 + groups.size()) % groups.size();
        syncToEmi();
        save();
    }

    public CosmicBookmarkGroup createGroup(String name) {
        CosmicBookmarkGroup group = new CosmicBookmarkGroup(name);
        groups.add(group);
        save();
        return group;
    }

    public void deleteGroup(int index) {
        if (groups.size() <= 1) {
            groups.get(0).clear();
            return;
        }

        groups.remove(index);
        if (activeGroupIndex >= groups.size()) {
            activeGroupIndex = groups.size() - 1;
        }
        syncToEmi();
        save();
    }

    public void addFavoriteWithAmount(EmiIngredient stack, EmiRecipe recipe, long amount) {
        CosmicFavorite favorite = CosmicFavorite.withAmount(stack, recipe, amount);
        getActiveGroup().addFavorite(favorite);
        syncToEmi();
        save();
    }

    public void addFavorite(EmiIngredient stack, EmiRecipe recipe) {
        EmiFavorite favorite = new EmiFavorite(stack, recipe);
        getActiveGroup().addFavorite(favorite);
        syncToEmi();
        save();
    }

    public void addFavoriteAt(EmiFavorite favorite, int index) {
        getActiveGroup().addFavoriteAt(favorite, index);
        syncToEmi();
        save();
    }

    public boolean removeFavorite(EmiIngredient stack) {
        boolean removed = getActiveGroup().removeFavorite(stack);
        if (removed) {
            syncToEmi();
            save();
        }
        return removed;
    }

    public boolean adjustFavoriteAmount(int index, long delta) {
        List<EmiFavorite> favorites = getActiveGroup().getFavorites();
        if (index < 0 || index >= favorites.size()) {
            return false;
        }

        EmiFavorite favorite = favorites.get(index);

        if (favorite instanceof CosmicFavorite cosmic) {
            cosmic.adjustAmount(delta);
        } else {
            long currentAmount = favorite.getStack().getAmount();
            long newAmount = Math.max(1, currentAmount + delta);
            CosmicFavorite newFavorite = CosmicFavorite.withAmount(
                    favorite.getStack(), favorite.getRecipe(), newAmount);
            favorites.set(index, newFavorite);
        }

        syncToEmi();
        save();
        return true;
    }

    public EmiFavorite getFavoriteAt(int index) {
        List<EmiFavorite> favorites = getActiveGroup().getFavorites();
        if (index < 0 || index >= favorites.size()) {
            return null;
        }
        return favorites.get(index);
    }

    public int findFavoriteIndex(EmiIngredient stack) {
        List<EmiFavorite> favorites = getActiveGroup().getFavorites();
        for (int i = 0; i < favorites.size(); i++) {
            if (favorites.get(i).strictEquals(stack)) {
                return i;
            }
        }
        return -1;
    }

    public void renameActiveGroup(String newName) {
        getActiveGroup().setName(newName);
        save();
    }

    public void renameGroup(int index, String newName) {
        if (index >= 0 && index < groups.size()) {
            groups.get(index).setName(newName);
            save();
        }
    }

    public void toggleViewMode() {
        getActiveGroup().toggleViewMode();
        save();
    }

    public CosmicBookmarkGroup.ViewMode getActiveViewMode() {
        return getActiveGroup().getViewMode();
    }

    public boolean bookmarkRecipeWithInputs(EmiRecipe recipe) {
        if (recipe == null) {
            return false;
        }

        CosmicRecipeBookmark bookmark = CosmicRecipeBookmark.fromRecipe(recipe);
        if (bookmark == null) {
            return false;
        }

        CosmicBookmarkGroup group = getActiveGroup();
        group.addRecipeBookmark(bookmark);

        if (group.getViewMode() != CosmicBookmarkGroup.ViewMode.TODO_LIST) {
            group.setViewMode(CosmicBookmarkGroup.ViewMode.TODO_LIST);
        }

        save();
        return true;
    }

    public List<CosmicRecipeBookmark> getActiveRecipeBookmarks() {
        return getActiveGroup().getRecipeBookmarks();
    }

    public boolean adjustRecipeMultiplier(int recipeIndex, long delta) {
        CosmicRecipeBookmark bookmark = getActiveGroup().getRecipeBookmark(recipeIndex);
        if (bookmark != null) {
            bookmark.adjustMultiplier(delta);
            save();
            return true;
        }
        return false;
    }

    public boolean removeRecipeBookmark(int index) {
        boolean removed = getActiveGroup().removeRecipeBookmark(index);
        if (removed) {
            save();
        }
        return removed;
    }

    public void syncToEmi() {
        EmiFavorites.favorites.clear();
        EmiFavorites.favorites.addAll(getActiveGroup().getFavorites());
    }

    public void syncFromEmi() {
        CosmicBookmarkGroup active = getActiveGroup();
        active.getFavorites().clear();
        active.getFavorites().addAll(EmiFavorites.favorites);
        save();
    }

    private File getSaveFile() {
        File configDir = new File(Minecraft.getInstance().gameDirectory, "config");
        File cosmicDir = new File(configDir, "cosmiccore");
        if (!cosmicDir.exists()) {
            cosmicDir.mkdirs();
        }
        return new File(cosmicDir, FILENAME);
    }

    public void save() {
        try {
            File saveFile = getSaveFile();
            JsonObject root = new JsonObject();
            root.addProperty("activeGroup", activeGroupIndex);

            JsonArray groupsArr = new JsonArray();
            for (CosmicBookmarkGroup group : groups) {
                groupsArr.add(group.toJson());
            }
            root.add("groups", groupsArr);

            try (FileWriter writer = new FileWriter(saveFile)) {
                GSON.toJson(root, writer);
            }
        } catch (Exception e) {
            CosmicCore.LOGGER.error("Failed to save cosmic bookmarks", e);
        }
    }

    public void load() {
        File file = getSaveFile();
        if (!file.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null) {
                return;
            }

            groups.clear();

            JsonArray groupsArr = root.getAsJsonArray("groups");
            if (groupsArr != null) {
                for (JsonElement el : groupsArr) {
                    if (el.isJsonObject()) {
                        groups.add(CosmicBookmarkGroup.fromJson(el.getAsJsonObject()));
                    }
                }
            }

            if (groups.isEmpty()) {
                groups.add(new CosmicBookmarkGroup("Default"));
            }

            activeGroupIndex = root.has("activeGroup") ? root.get("activeGroup").getAsInt() : 0;
            if (activeGroupIndex >= groups.size()) {
                activeGroupIndex = 0;
            }

            syncToEmi();

        } catch (Exception e) {
            CosmicCore.LOGGER.error("Failed to load cosmic bookmarks from {}", file, e);
            if (groups.isEmpty()) {
                groups.add(new CosmicBookmarkGroup("Default"));
            }
        }
    }

    public void reload() {
        load();
    }

    public static void reset() {
        if (instance != null) {
            instance.save();
            instance = null;
        }
    }

    public static void init() {
        getInstance();
    }
}
