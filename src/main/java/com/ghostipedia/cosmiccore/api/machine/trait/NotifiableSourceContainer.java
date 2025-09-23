package com.ghostipedia.cosmiccore.api.machine.trait;

import com.ghostipedia.cosmiccore.api.capability.recipe.SourceRecipeCapability;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import com.hollingsworth.arsnouveau.api.source.ISourceTile;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class NotifiableSourceContainer extends NotifiableRecipeHandlerTrait<Integer> implements ISourceTile {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            NotifiableSourceContainer.class,
            NotifiableRecipeHandlerTrait.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    private int currentSource;

    @Persisted
    private int maxSource;

    @Persisted
    private int maxConsumption;

    private final IO handlerIO;

    public NotifiableSourceContainer(MetaMachine machine, IO io, int maxCapacity, int maxConsumption) {
        super(machine);
        this.setMaxSource(maxSource);
        this.maxConsumption = maxConsumption;
        this.handlerIO = io;
    }

    @Override
    public IO getHandlerIO() {
        return handlerIO;
    }

    @Override
    public List<Integer> handleRecipeInner(IO io, GTRecipe recipe, List<Integer> left, boolean simulate) {
        int source = left.stream().reduce(0, Integer::sum);
        if (io == IO.IN) {
            var canOutput = Math.min(maxConsumption, currentSource);
            if (!simulate) source = addSource(Math.min(canOutput, source));
            source -= canOutput;
        } else if (io == IO.OUT) {
            var canInput = maxSource;
            if (!simulate) source = addSource(Math.max(canInput, maxSource));
            source -= canInput;
        }
        return source <= 0 ? null : Collections.singletonList(source);
    }

    @Override
    public @NotNull List<Object> getContents() {
        return List.of(currentSource);
    }

    @Override
    public double getTotalContentAmount() {
        return currentSource;
    }

    @Override
    public RecipeCapability<Integer> getCapability() {
        return SourceRecipeCapability.CAP;
    }

    @Override
    public int getTransferRate() {
        return maxConsumption;
    }

    @Override
    public boolean canAcceptSource() {
        return currentSource < maxSource;
    }

    @Override
    public int getSource() {
        return currentSource;
    }

    @Override
    public int getMaxSource() {
        return maxSource;
    }

    @Override
    public void setMaxSource(int max) {
        this.maxSource = max;
    }

    @Override
    public int setSource(int source) {
        if (this.currentSource <= source) {
            return this.currentSource = source;
        } else {
            return this.currentSource = this.maxSource;
        }
    }

    @Override
    public int addSource(int source) {
        return handlerIO == IO.IN ? this.setSource(currentSource + source) : this.currentSource;
    }

    @Override
    public int removeSource(int source) {
        return currentSource -= source;
    }
}
