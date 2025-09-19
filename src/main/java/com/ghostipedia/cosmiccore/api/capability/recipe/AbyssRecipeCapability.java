package com.ghostipedia.cosmiccore.api.capability.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.IContentSerializer;
import com.gregtechceu.gtceu.api.recipe.content.SerializerInteger;

public class AbyssRecipeCapability extends RecipeCapability<Integer> {

    protected AbyssRecipeCapability(String name, int color, boolean doRenderSlot, int sortIndex, IContentSerializer<Integer> serializer) {
        super("abyss", 0x301934,true, 9, SerializerInteger.INSTANCE );
    }

    @Override
    public boolean isRecipeSearchFilter() {
        return true;
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
