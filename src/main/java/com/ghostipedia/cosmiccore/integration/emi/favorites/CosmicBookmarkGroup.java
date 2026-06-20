package com.ghostipedia.cosmiccore.integration.emi.favorites;

import dev.emi.emi.runtime.EmiFavorite;

import java.util.ArrayList;
import java.util.List;

public class CosmicBookmarkGroup {

    public enum GroupType {
        REGULAR,
        RECIPE
    }

    private String name;
    private GroupType type;
    private List<EmiFavorite> favorites;

    public CosmicBookmarkGroup(String name) {
        this(name, GroupType.REGULAR);
    }

    public CosmicBookmarkGroup(String name, GroupType type) {
        this.name = name;
        this.type = type;
        this.favorites = new ArrayList<>();
    }

    public CosmicBookmarkGroup(String name, GroupType type, List<EmiFavorite> favorites) {
        this.name = name;
        this.type = type;
        this.favorites = new ArrayList<>(favorites);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GroupType getType() {
        return type;
    }

    public void setType(GroupType type) {
        this.type = type;
    }

    public boolean isRecipeGroup() {
        return type == GroupType.RECIPE;
    }

    public List<EmiFavorite> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<EmiFavorite> favorites) {
        this.favorites = new ArrayList<>(favorites);
    }

    public void clear() {
        favorites.clear();
    }

    public int size() {
        return favorites.size();
    }
}
