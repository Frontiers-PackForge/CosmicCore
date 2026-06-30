package com.ghostipedia.cosmiccore.api.capability.recipe;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerFluidIngredient;

import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class SterileRecipeCapability extends RecipeCapability<SizedFluidIngredient> {

    public final static SterileRecipeCapability CAP = new SterileRecipeCapability();

    protected SterileRecipeCapability() {
        super(CosmicCore.id("sterile"), 0x5E2129FF, true, 10, SerializerFluidIngredient.INSTANCE);
    }

    @Override
    public SizedFluidIngredient copyInner(SizedFluidIngredient content) {
        return FluidRecipeCapability.CAP.copyInner(content);
    }

    @Override
    public SizedFluidIngredient copyWithModifier(SizedFluidIngredient content, ContentModifier modifier) {
        return FluidRecipeCapability.CAP.copyWithModifier(content, modifier);
    }

    @Override
    public boolean isRecipeSearchFilter() {
        return true;
    }

    // TODO(8.0.0): FluidIngredient -> SizedFluidIngredient. Re-add the MapSterileIngredient lookup
    // (getDefaultMapIngredient) + XEI display once SterilizationHatch/NotifiableSterileTank are reconciled.
}
