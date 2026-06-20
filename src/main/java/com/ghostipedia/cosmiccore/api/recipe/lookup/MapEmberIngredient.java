package com.ghostipedia.cosmiccore.api.recipe.lookup;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import java.util.Collections;
import java.util.List;

public class MapEmberIngredient extends AbstractMapIngredient {

    public final Double embers;

    public MapEmberIngredient(Double embers) {
        this.embers = embers;
    }

    @Override
    protected int hash() {
        return MapEmberIngredient.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MapEmberIngredient;
    }

    @Override
    public String toString() {
        return "MapEmberIngredient{" + "embers=" + embers + '}';
    }

    public static List<AbstractMapIngredient> convertToMapIngredient(Double embers) {
        return Collections.singletonList(new MapEmberIngredient(embers));
    }
}
