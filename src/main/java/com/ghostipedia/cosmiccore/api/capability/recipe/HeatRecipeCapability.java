package com.ghostipedia.cosmiccore.api.capability.recipe;

import com.ghostipedia.cosmiccore.api.recipe.lookup.MapHeatIngredient;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerLong;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

public class HeatRecipeCapability extends RecipeCapability<Long> {

    public final static HeatRecipeCapability CAP = new HeatRecipeCapability();

    protected HeatRecipeCapability() {
        super("thermia", 0x5E2129FF, true, 11, SerializerLong.INSTANCE);
    }

    @Override
    public Long copyInner(Long content) {
        return content;
    }

    @Override
    public Long copyWithModifier(Long content, ContentModifier modifier) {
        return modifier.apply(content);
    }

    // @Override
    public List<AbstractMapIngredient> convertToMapIngredient(Object ingredient) {
        List<AbstractMapIngredient> ingredients = new ObjectArrayList<>(1);
        if (ingredient instanceof Long thermia) ingredients.add(new MapHeatIngredient(thermia));
        return ingredients;
    }

    @Override
    public boolean isRecipeSearchFilter() {
        return true;
    }

    // TODO(8.0.0): re-add XEI display via the new XEI category API.
    // RecipeCapability#addXEIInfo was removed in 8.0.0; the original LDLib LabelWidget rendering
    // (thermiaIn / thermiaOut) lived here and needs reimplementing against the new XEI category hook.
}
