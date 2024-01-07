package com.ghostipedia.cosmiccore;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.trait.NotifiableStressTrait;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.lowdragmc.lowdraglib.misc.FluidTransferList;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;




public class BasicAirMachineLogic extends RecipeLogic {

    @Nullable @Getter
    public FluidTransferList inputFluidInventory;

    public BasicAirMachineLogic(BasicAirMachine machine) {
        super(machine);
    }

    @Override
    public BasicAirMachine getMachine() {
        return (BasicAirMachine)super.getMachine();
    }
}

