package com.ghostipedia.cosmiccore.common.machine.multiblock.electric.modular;

import com.ghostipedia.cosmiccore.api.machine.multiblock.modular.WorkableElectricMultiblockMachineModule;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import com.lowdragmc.lowdraglib.utils.DummyWorld;

import java.util.List;

public class ModuleTest extends WorkableElectricMultiblockMachineModule {

    public ModuleTest(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        super.setWorkingEnabled(isWorkingAllowed);
        notifyBases();
    }

    @Override
    public void addCapabilitiesFromBase(List<IoRecipeCapability> capabilitiesToExtract) {
        super.addCapabilitiesFromBase(capabilitiesToExtract);
        capabilitiesToExtract.add(new IoRecipeCapability(IO.IN, EURecipeCapability.CAP));
        capabilitiesToExtract.add(new IoRecipeCapability(IO.OUT, EURecipeCapability.CAP));
    }
}
