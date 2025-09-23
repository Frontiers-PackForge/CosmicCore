package com.ghostipedia.cosmiccore.api.recipe.lookup;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import java.util.Collections;
import java.util.List;

public class MapSourceIngredient extends AbstractMapIngredient {

    public final Integer source;

    public MapSourceIngredient(Integer source) {
        this.source = source;
    }

    @Override
    protected int hash() {
        return MapSourceIngredient.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MapSourceIngredient;
    }

    @Override
    public String toString() {
        return "MapSourceIngredient{" + "source=" + source + '}';
    }

    public static List<AbstractMapIngredient> convertToMapIngredient(Integer source) {
        return Collections.singletonList(new MapSourceIngredient(source));
    }
}
