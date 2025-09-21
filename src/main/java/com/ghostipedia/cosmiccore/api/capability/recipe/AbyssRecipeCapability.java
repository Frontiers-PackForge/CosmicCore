package com.ghostipedia.cosmiccore.api.capability.recipe;

import com.ghostipedia.cosmiccore.api.recipe.lookup.MapAbyssIngredient;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerInteger;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import kroppeb.stareval.function.Type;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class AbyssRecipeCapability extends RecipeCapability<Integer> {

    public final static AbyssRecipeCapability CAP = new AbyssRecipeCapability();

    protected AbyssRecipeCapability() {
        super("abyss", 0x301934,true, 10, SerializerInteger.INSTANCE );
    }

    @Override
    public @Nullable List<AbstractMapIngredient> getDefaultMapIngredient(Object object) {

        List<AbstractMapIngredient> ingredients = new ObjectArrayList<>(1);
        if (object instanceof Integer Abyss) {
            ingredients.add(new MapAbyssIngredient(Abyss));
        }
        return ingredients;
    }

    @Override
    public List<Object> compressIngredients(Collection<Object> ingredients) {
        return super.compressIngredients(ingredients);
    }

    @Override
    public Integer copyInner(Integer content) {
        return content;
    }

    @Override
    public Integer copyWithModifier(Integer content, ContentModifier modifier) {
        return modifier.apply(content);
    }
}
