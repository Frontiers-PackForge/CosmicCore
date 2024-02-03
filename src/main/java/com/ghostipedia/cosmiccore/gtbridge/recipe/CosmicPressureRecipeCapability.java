package com.ghostipedia.cosmiccore.gtbridge.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerDouble;

public class CosmicPressureRecipeCapability extends RecipeCapability<Double> {

    public static final CosmicPressureRecipeCapability CAP = new CosmicPressureRecipeCapability();


    protected CosmicPressureRecipeCapability() {
        super("bar", 0xFFAA33AA, SerializerDouble.INSTANCE);
    }

    @Override
    public Double copyInner(Double content) {
        return content;
    }

    public Double copyWithModifier(Double content, ContentModifier modifier){
        return modifier.apply(content).doubleValue();
    }
}
