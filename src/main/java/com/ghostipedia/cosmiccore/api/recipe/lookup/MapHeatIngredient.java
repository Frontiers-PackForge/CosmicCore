package com.ghostipedia.cosmiccore.api.recipe.lookup;

import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;

public class MapHeatIngredient extends AbstractMapIngredient {

    public final long thermia;

    public MapHeatIngredient(Long temp) {
        this.thermia = temp;
    }

    @Override
    protected int hash() {
        return MapHeatIngredient.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MapHeatIngredient;
    }

    @Override
    public String toString() {
        return "MapHeatIngredient{" + "thermia=" + thermia + '}';
    }
}
