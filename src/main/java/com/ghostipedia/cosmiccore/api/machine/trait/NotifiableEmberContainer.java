package com.ghostipedia.cosmiccore.api.machine.trait;

import com.ghostipedia.cosmiccore.api.capability.recipe.EmberRecipeCapability;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import com.rekindled.embers.api.capabilities.EmbersCapabilities;
import com.rekindled.embers.api.power.IEmberCapability;
import com.rekindled.embers.power.DefaultEmberCapability;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class NotifiableEmberContainer extends NotifiableRecipeHandlerTrait<Double> implements ICapabilityProvider {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(NotifiableEmberContainer.class,
            NotifiableRecipeHandlerTrait.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    public IEmberCapability capability = new DefaultEmberCapability() {
        @Override
        public void onContentsChanged() {
            super.onContentsChanged();
            notifyListeners();
        }
    };

    @Override
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        capability.writeToNBT(tag);
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        capability.deserializeNBT(tag);
        if (capability.getEmberCapacity() == 0)
            capability.setEmberCapacity(maxCapacity);
    }

    private final IO handlerIO;

    @Persisted
    private double maxCapacity;

    @Persisted
    private double maxConsumption;

    public NotifiableEmberContainer(MetaMachine machine, IO io, double maxCapacity, double maxConsumption) {
        super(machine);
        this.capability.setEmberCapacity(maxCapacity);
        this.capability.setEmber(0.0D);
        this.handlerIO = io;
        this.maxCapacity = maxCapacity;
        this.maxConsumption = maxConsumption;
    }

    @Override
    public IO getHandlerIO() {
        return handlerIO;
    }

    @Override
    public List<Double> handleRecipeInner(IO io, GTRecipe recipe, List<Double> left, boolean simulate) {
        double ember = left.stream().reduce(0.0D, Double::sum);
        if (io == IO.IN) {
            var canOutput = Math.min(maxConsumption, capability.getEmber());
            if (!simulate) ember = capability.removeAmount(Math.min(canOutput, ember), true);
            ember -= canOutput;
        } else if (io == IO.OUT) {
            var canInput = maxCapacity - capability.getEmber();
            if (!simulate) ember = capability.addAmount(Math.min(canInput, ember), true);
            ember -= canInput;
        }
        return ember <= 0 ? null : Collections.singletonList(ember);
    }

    @Override
    public @NotNull List<Object> getContents() {
        return List.of(capability.getEmber());
    }

    @Override
    public double getTotalContentAmount() {
        return capability.getEmber();
    }

    @Override
    public RecipeCapability<Double> getCapability() {
        return EmberRecipeCapability.CAP;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction direction) {
        if (cap == EmbersCapabilities.EMBER_CAPABILITY) {
            return capability.getCapability(cap, direction);
        }
        return this.getCapability(cap);
    }
}
