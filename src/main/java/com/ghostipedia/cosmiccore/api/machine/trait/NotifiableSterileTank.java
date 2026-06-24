package com.ghostipedia.cosmiccore.api.machine.trait;

import com.ghostipedia.cosmiccore.api.capability.recipe.SterileRecipeCapability;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;

import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class NotifiableSterileTank extends NotifiableFluidTank {

    public NotifiableSterileTank(MetaMachine machine, int slots, int capacity, IO io, IO capabilityIO) {
        super(slots, capacity, io, capabilityIO);
        machine.attachTrait(this);
    }

    @Override
    public RecipeCapability<SizedFluidIngredient> getCapability() {
        return SterileRecipeCapability.CAP;
    }
}
