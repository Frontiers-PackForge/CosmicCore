package com.ghostipedia.cosmiccore.api.recipe.lookup;

import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;

public class MapSoulIngredient extends AbstractMapIngredient {

    public final Integer souls;

    public MapSoulIngredient(Integer souls) {
        this.souls = souls;
    }

    @Override
    protected int hash() {
        return MapSoulIngredient.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MapSoulIngredient;
    }

    @Override
    public String toString() {
        return "MapSoulIngredient{" + "souls=" + souls + '}';
    }
}
