package com.ghostipedia.cosmiccore.common.machine.multiblock.part;

import com.ghostipedia.cosmiccore.common.wireless.WirelessDataStore;
import com.ghostipedia.cosmiccore.utils.OwnershipUtils;

import com.gregtechceu.gtceu.api.capability.IDataAccessHatch;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.recipe.condition.ResearchCondition;

import net.minecraft.MethodsReturnNonnullByDefault;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WirelessDataHatchPartMachine extends MultiblockPartMachine
                                          implements IDataAccessHatch {

    public WirelessDataHatchPartMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public GTRecipe modifyRecipe(GTRecipe recipe) {
        return IDataAccessHatch.super.modifyRecipe(recipe);
    }

    @Override
    public boolean isCreative() {
        return false;
    }

    @Override
    public boolean isRecipeAvailable(@NotNull GTRecipe recipe, @NotNull Collection<IDataAccessHatch> seen) {
        seen.add(this);
        var dataStore = WirelessDataStore.getWirelessDataStore(OwnershipUtils.getOwnerUUID(getHolder().getOwner()));
        return recipe.conditions.stream().noneMatch(ResearchCondition.class::isInstance) ||
                dataStore.isRecipeAvailable(recipe, seen);
    }
}
