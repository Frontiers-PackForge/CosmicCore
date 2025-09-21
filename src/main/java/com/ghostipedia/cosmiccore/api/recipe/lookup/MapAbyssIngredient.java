package com.ghostipedia.cosmiccore.api.recipe.lookup;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import lombok.ToString;

public class MapAbyssIngredient extends AbstractMapIngredient {

    public final Integer Corruption;

    public MapAbyssIngredient(Integer corruption) {
        Corruption = corruption;
    }

    @Override
    protected int hash() {
        return MapAbyssIngredient.class.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return  o instanceof MapAbyssIngredient;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public boolean isSpecialIngredient() {
        return true;
    }
}
