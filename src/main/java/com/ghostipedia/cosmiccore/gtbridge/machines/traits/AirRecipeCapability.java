package com.ghostipedia.cosmiccore.gtbridge.machines.traits;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerDouble;

public class AirRecipeCapability extends RecipeCapability<Double> {
    public static final AirRecipeCapability CAP = new AirRecipeCapability();


    protected AirRecipeCapability() {
        super("Bar", 0xFFAA33AA, SerializerDouble.INSTANCE);
    }

    @Override
    public Double copyInner(Double content) {
        return content;
    }

    public Double copyWithModifier(Double content, ContentModifier modifier){
        return modifier.apply(content).doubleValue();
    }
}
