package com.ghostipedia.cosmiccore.common.machine.vitae;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;

import java.util.List;
import java.util.function.Supplier;

public final class VitaeCampusEnergyHandler extends NotifiableRecipeHandlerTrait<EnergyStack> {

    private final Supplier<IEnergyContainer> source;

    public VitaeCampusEnergyHandler(Supplier<IEnergyContainer> source) {
        this.source = source;
    }

    @Override
    public IO getHandlerIO() {
        return IO.IN;
    }

    @Override
    public RecipeCapability<EnergyStack> getCapability() {
        return EURecipeCapability.CAP;
    }

    @Override
    public List<EnergyStack> handleRecipeInner(IO io, GTRecipe recipe, List<EnergyStack> left, boolean simulate) {
        if (io != IO.IN) return left;
        IEnergyContainer energy = source.get();
        if (energy == null) return left;
        long requested = 0;
        try {
            for (EnergyStack stack : left) {
                if (stack.voltage() < 0 || stack.amperage() < 1) return left;
                requested = Math.addExact(requested, Math.multiplyExact(stack.voltage(), stack.amperage()));
            }
        } catch (ArithmeticException overflow) {
            return left;
        }
        if (energy.getEnergyStored() < requested) return left;
        if (!simulate && requested > 0 && energy.removeEnergy(requested) != requested) return left;
        left.clear();
        return left;
    }

    @Override
    public List<Object> getContents() {
        IEnergyContainer energy = source.get();
        return energy == null ? List.of() : List.of(new EnergyStack(energy.getEnergyStored()));
    }

    @Override
    public double getTotalContentAmount() {
        IEnergyContainer energy = source.get();
        return energy == null ? 0 : energy.getEnergyStored();
    }
}
