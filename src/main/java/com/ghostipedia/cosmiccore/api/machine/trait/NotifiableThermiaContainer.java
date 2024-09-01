package com.ghostipedia.cosmiccore.api.machine.trait;

import com.ghostipedia.cosmiccore.api.capability.recipe.IHeatContainer;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import lombok.Getter;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NotifiableThermiaContainer extends NotifiableRecipeHandlerTrait<Integer> implements IHeatContainer {
    @Getter
    private final IO handlerIO;
    @Getter
    private final long overloadLimit;
    @Getter
    private final long currentTemp;
    public NotifiableThermiaContainer(MetaMachine machine, IO io, long overloadLimit, long currentTemp) {
        super(machine);
        this.handlerIO = io;
        this.overloadLimit = overloadLimit;
        this.currentTemp = currentTemp;
    }

    @Override
    public long acceptHeatFromNetwork(Direction side) {
        return 0;
    }

    @Override
    public boolean inputsHeat(Direction side) {
        return false;
    }

    @Override
    public long changeHeat(long heatDifference) {
        return 0;
    }

    @Override
    public long getHeatStorage() {
        return this.getHeatInfo().stored();
    }

    @Override
    public List<Integer> handleRecipeInner(IO io, GTRecipe recipe, List<Integer> left, @Nullable String slotName, boolean simulate) {
        return null;
    }

    @Override
    public List<Object> getContents() {
        return null;
    }

    @Override
    public double getTotalContentAmount() {
        return 0;
    }

    @Override
    public RecipeCapability<Integer> getCapability() {
        return null;
    }
}
