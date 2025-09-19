package com.ghostipedia.cosmiccore.api.recipe.lookup;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

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
    public boolean isSpecialIngredient() {
        return true;
    }
}
