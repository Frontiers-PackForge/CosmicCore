package com.ghostipedia.cosmiccore.api.capability.recipe;

import com.ghostipedia.cosmiccore.api.recipe.lookup.MapSoulIngredient;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerInteger;
import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Collection;
import java.util.List;

public class SoulRecipeCapability extends RecipeCapability<Integer> {
    public final static SoulRecipeCapability CAP = new SoulRecipeCapability();

    protected SoulRecipeCapability() {
        super("soul", 0x5E2129FF, true, 0, SerializerInteger.INSTANCE);
    }

    @Override
    public Integer copyInner(Integer content) {
        return content;
    }

    @Override
    public Integer copyWithModifier(Integer content, ContentModifier modifier) {
        return modifier.apply(content).intValue();
    }

    @Override
    public List<AbstractMapIngredient> convertToMapIngredient(Object ingredient) {
        List<AbstractMapIngredient> ingredients = new ObjectArrayList<>(1);
        if (ingredient instanceof Integer essence) ingredients.add(new MapSoulIngredient(essence));
        return ingredients;
    }

    @Override
    public List<Object> compressIngredients(Collection<Object> ingredients) {
        //TODO: Figure out what it needs to do
        return super.compressIngredients(ingredients);
    }

    @Override
    public boolean isRecipeSearchFilter() {
        return true;
    }
}
