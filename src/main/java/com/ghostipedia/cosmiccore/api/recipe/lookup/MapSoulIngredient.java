package com.ghostipedia.cosmiccore.api.recipe.lookup;

import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulIngredient;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulStack;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import java.util.List;

public class MapSoulIngredient extends AbstractMapIngredient {

    public final SoulStack stack;

    public MapSoulIngredient(SoulStack stack) {
        this.stack = stack;
    }

    @Override
    protected int hash() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MapSoulIngredient other)) return false;
        return stack.equals(other.stack);
    }

    public static List<AbstractMapIngredient> from(SoulIngredient soulIngredient) {
        SoulStack stack = soulIngredient.stack();
        return List.of(new MapSoulIngredient(stack));
    }

    @Override
    public String toString() {
        return "MapSoulIngredient{stack=" + stack + "}";
    }
}
