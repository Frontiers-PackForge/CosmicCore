package com.ghostipedia.cosmiccore.api.machine.trait;

import com.ghostipedia.cosmiccore.api.capability.recipe.SterileRecipeCapability;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;

public class NotifiableSterileTank extends NotifiableFluidTank {

    public NotifiableSterileTank(MetaMachine machine, int slots, int capacity, IO io, IO capabilityIO) {
        super(machine, slots, capacity, io, capabilityIO);
    }

    @Override
    public RecipeCapability<FluidIngredient> getCapability() {
        return SterileRecipeCapability.CAP;
    }
}
