package com.ghostipedia.cosmiccore.integration.emi.favorites;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.integration.emi.CosmicFavorite;
import com.ghostipedia.cosmiccore.integration.emi.CosmicRecipeFavorite;

import net.minecraft.client.Minecraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import dev.emi.emi.runtime.EmiFavorite;
import dev.emi.emi.runtime.EmiFavorites;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CosmicBookmarkManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static CosmicBookmarkManager instance;

    private List<CosmicBookmarkGroup> groups = new ArrayList<>();
    private int activeIndex = 0;

    private CosmicBookmarkManager() {
        groups.add(new CosmicBookmarkGroup("Default"));
    }

    public static CosmicBookmarkManager getInstance() {
        if (instance == null) {
            instance = new CosmicBookmarkManager();
        }
        return instance;
    }

    public int getActiveIndex() {
        return activeIndex;
    }

    public int getGroupCount() {
        return groups.size();
    }

    public CosmicBookmarkGroup getActiveGroup() {
        if (activeIndex >= 0 && activeIndex < groups.size()) {
            return groups.get(activeIndex);
        }
        return groups.get(0);
    }

    public CosmicBookmarkGroup getGroupAt(int index) {
        if (index >= 0 && index < groups.size()) {
            return groups.get(index);
        }
        return null;
    }

    public void nextGroup() {
        saveCurrentToGroup();
        activeIndex = (activeIndex + 1) % groups.size();
        loadGroupToEmi();
    }

    public void prevGroup() {
        saveCurrentToGroup();
        activeIndex = (activeIndex - 1 + groups.size()) % groups.size();
        loadGroupToEmi();
    }

    public void setActiveIndex(int index) {
        if (index >= 0 && index < groups.size() && index != activeIndex) {
            saveCurrentToGroup();
            activeIndex = index;
            loadGroupToEmi();
        }
    }

    public void addGroup(String name) {
        addGroup(name, CosmicBookmarkGroup.GroupType.REGULAR);
    }

    public void addGroup(String name, CosmicBookmarkGroup.GroupType type) {
        saveCurrentToGroup();
        groups.add(new CosmicBookmarkGroup(name, type));
        activeIndex = groups.size() - 1;
        loadGroupToEmi();
    }

    public void removeGroup(int index) {
        if (groups.size() <= 1) return;
        if (index < 0 || index >= groups.size()) return;

        groups.remove(index);
        if (activeIndex >= groups.size()) {
            activeIndex = groups.size() - 1;
        }
        loadGroupToEmi();
    }

    public void saveCurrentToGroup() {
        CosmicBookmarkGroup group = getActiveGroup();
        group.setFavorites(new ArrayList<>(EmiFavorites.favorites));
    }

    public void loadGroupToEmi() {
        CosmicBookmarkGroup group = getActiveGroup();
        EmiFavorites.favorites.clear();
        EmiFavorites.favorites.addAll(group.getFavorites());
    }

    /**
     * Calculate how many pages are needed to display all recipes in the current group.
     * Only meaningful for recipe groups.
     */
    public int getRecipePageCount(int gridWidth, int gridHeight) {
        if (!getActiveGroup().isRecipeGroup() || gridWidth <= 0 || gridHeight <= 0) {
            return 1;
        }

        List<CosmicRecipeFavorite> recipes = EmiFavorites.favorites.stream()
                .filter(f -> f instanceof CosmicRecipeFavorite)
                .map(f -> (CosmicRecipeFavorite) f)
                .toList();

        if (recipes.isEmpty()) return 1;

        int pageCount = 1;
        int rowsUsed = 0;

        for (CosmicRecipeFavorite recipe : recipes) {
            int rowsNeeded = recipe.getRowCount(gridWidth);

            if (rowsUsed + rowsNeeded > gridHeight && rowsUsed > 0) {
                pageCount++;
                rowsUsed = 0;
            }
            rowsUsed += rowsNeeded;
        }

        return pageCount;
    }

    public Path getSavePath() {
        return Minecraft.getInstance().gameDirectory.toPath()
                .resolve("config")
                .resolve("cosmiccore")
                .resolve("cosmic_bookmarks.json");
    }

    public void save() {
        saveCurrentToGroup();

        JsonObject root = new JsonObject();
        root.addProperty("activeGroup", activeIndex);

        JsonArray groupsArray = new JsonArray();
        for (CosmicBookmarkGroup group : groups) {
            JsonObject groupObj = new JsonObject();
            groupObj.addProperty("name", group.getName());
            groupObj.addProperty("type", group.getType().name());

            JsonArray favoritesArray = new JsonArray();
            for (EmiFavorite fav : group.getFavorites()) {
                JsonObject favObj = new JsonObject();
                JsonElement stack = EmiIngredientSerializer.getSerialized(fav.getStack());
                if (stack != null) {
                    favObj.add("stack", stack);

                    if (fav instanceof CosmicRecipeFavorite recipe) {
                        favObj.addProperty("isRecipe", true);
                        favObj.addProperty("outputAmount", recipe.getOutputAmount());
                        JsonArray inputsArray = new JsonArray();
                        for (CosmicRecipeFavorite.InputEntry input : recipe.getInputs()) {
                            JsonObject inputObj = new JsonObject();
                            JsonElement inputStack = EmiIngredientSerializer.getSerialized(input.stack());
                            if (inputStack != null) {
                                inputObj.add("stack", inputStack);
                                inputObj.addProperty("amount", input.amount());
                                inputsArray.add(inputObj);
                            }
                        }
                        favObj.add("inputs", inputsArray);
                    } else if (fav instanceof CosmicFavorite cosmic) {
                        favObj.addProperty("cosmicAmount", cosmic.getAmount());
                    }

                    if (fav.getRecipe() != null && fav.getRecipe().getId() != null) {
                        favObj.addProperty("recipe", fav.getRecipe().getId().toString());
                    }
                    favoritesArray.add(favObj);
                }
            }
            groupObj.add("favorites", favoritesArray);
            groupsArray.add(groupObj);
        }
        root.add("groups", groupsArray);

        try {
            Path path = getSavePath();
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(root));
        } catch (IOException e) {
            CosmicCore.LOGGER.error("Failed to save cosmic bookmarks", e);
        }
    }

    public void load() {
        Path path = getSavePath();
        if (!Files.exists(path)) {
            return;
        }

        try {
            String content = Files.readString(path);
            JsonObject root = JsonParser.parseString(content).getAsJsonObject();

            groups.clear();
            activeIndex = root.has("activeGroup") ? root.get("activeGroup").getAsInt() : 0;

            if (root.has("groups")) {
                JsonArray groupsArray = root.getAsJsonArray("groups");
                for (JsonElement groupEl : groupsArray) {
                    JsonObject groupObj = groupEl.getAsJsonObject();
                    String name = groupObj.has("name") ? groupObj.get("name").getAsString() : "Unnamed";
                    CosmicBookmarkGroup.GroupType type = CosmicBookmarkGroup.GroupType.REGULAR;
                    if (groupObj.has("type")) {
                        try {
                            type = CosmicBookmarkGroup.GroupType.valueOf(groupObj.get("type").getAsString());
                        } catch (IllegalArgumentException ignored) {}
                    }
                    CosmicBookmarkGroup group = new CosmicBookmarkGroup(name, type);

                    if (groupObj.has("favorites")) {
                        JsonArray favoritesArray = groupObj.getAsJsonArray("favorites");
                        List<EmiFavorite> favorites = new ArrayList<>();

                        for (JsonElement favEl : favoritesArray) {
                            JsonObject favObj = favEl.getAsJsonObject();
                            if (favObj.has("stack")) {
                                EmiIngredient ingredient = EmiIngredientSerializer.getDeserialized(favObj.get("stack"));
                                if (!ingredient.isEmpty()) {
                                    EmiFavorite fav;

                                    if (favObj.has("isRecipe") && favObj.get("isRecipe").getAsBoolean()) {
                                        long outputAmount = favObj.has("outputAmount") ?
                                                favObj.get("outputAmount").getAsLong() : 1;
                                        List<CosmicRecipeFavorite.InputEntry> inputs = new ArrayList<>();
                                        if (favObj.has("inputs")) {
                                            for (JsonElement inputEl : favObj.getAsJsonArray("inputs")) {
                                                JsonObject inputObj = inputEl.getAsJsonObject();
                                                if (inputObj.has("stack")) {
                                                    EmiIngredient inputStack = EmiIngredientSerializer
                                                            .getDeserialized(inputObj.get("stack"));
                                                    long inputAmount = inputObj.has("amount") ?
                                                            inputObj.get("amount").getAsLong() : 1;
                                                    if (!inputStack.isEmpty()) {
                                                        inputs.add(new CosmicRecipeFavorite.InputEntry(inputStack,
                                                                inputAmount));
                                                    }
                                                }
                                            }
                                        }
                                        fav = new CosmicRecipeFavorite(ingredient, outputAmount, inputs);
                                    } else if (favObj.has("cosmicAmount")) {
                                        long amount = favObj.get("cosmicAmount").getAsLong();
                                        fav = new CosmicFavorite(ingredient, amount);
                                    } else {
                                        fav = new EmiFavorite(ingredient, null);
                                    }
                                    favorites.add(fav);
                                }
                            }
                        }
                        group.setFavorites(favorites);
                    }
                    groups.add(group);
                }
            }

            if (groups.isEmpty()) {
                groups.add(new CosmicBookmarkGroup("Default"));
            }

            if (activeIndex >= groups.size()) {
                activeIndex = 0;
            }

            loadGroupToEmi();

        } catch (Exception e) {
            CosmicCore.LOGGER.error("Failed to load cosmic bookmarks", e);
            groups.clear();
            groups.add(new CosmicBookmarkGroup("Default"));
            activeIndex = 0;
        }
    }
}
