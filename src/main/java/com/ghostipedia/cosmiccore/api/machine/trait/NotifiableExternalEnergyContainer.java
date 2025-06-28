package com.ghostipedia.cosmiccore.api.machine.trait;

import com.ghostipedia.cosmiccore.api.misc.CosmicEnergyContainerList;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NotifiableExternalEnergyContainer extends NotifiableRecipeHandlerTrait<Long> implements IEnergyContainer {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            NotifiableExternalEnergyContainer.class, NotifiableRecipeHandlerTrait.MANAGED_FIELD_HOLDER);

    @Getter
    protected IO handlerIO;

    protected CosmicEnergyContainerList energyContainer;

    protected long amps, lastTimeStamp;

    @Nullable
    protected TickableSubscription outputSubs;
    @Nullable
    protected TickableSubscription updateSubs;

    public NotifiableExternalEnergyContainer(MetaMachine machine, CosmicEnergyContainerList containerList) {
        super(machine);
        var isIn = (containerList.getInputVoltage() != 0 && containerList.getInputAmperage() != 0);
        var isOut = (containerList.getOutputVoltage() != 0 && containerList.getOutputAmperage() != 0);
        this.handlerIO = (isIn && isOut) ? IO.BOTH : isIn ? IO.IN : isOut ? IO.OUT : IO.NONE;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }


    @Override
    public long acceptEnergyFromNetwork(Direction side, long voltage, long amperage) {
        return energyContainer.acceptEnergyFromNetwork(side, voltage, amperage);
    }

    @Override
    public boolean inputsEnergy(Direction side) {
        return energyContainer.inputsEnergy(side);
    }

    @Override
    public long changeEnergy(long differenceAmount) {
        return energyContainer.changeEnergy(differenceAmount);
    }

    @Override
    public long getEnergyStored() {
        return energyContainer.getEnergyStored();
    }

    @Override
    public long getEnergyCapacity() {
        return energyContainer.getEnergyCapacity();
    }

    public int getNumHighestInputContainers() {
        return energyContainer.getNumHighestInputContainers();
    }

    @Override
    public long getInputAmperage() {
        return energyContainer.getInputAmperage();
    }

    @Override
    public long getInputVoltage() {
        return energyContainer.getHighestInputVoltage();
    }

    @Override
    public IO getHandlerIO() {
        return this.handlerIO;
    }

    @Override
    public List<Long> handleRecipeInner(IO io, GTRecipe recipe, List<Long> left, boolean simulate) {
        //TODO: wtf is this
        return List.of();
    }

    @Override
    public @NotNull List<Object> getContents() {
        return List.of(energyContainer.getEnergyStored());
    }

    @Override
    public double getTotalContentAmount() {
        return energyContainer.getEnergyStored();
    }

    @Override
    public RecipeCapability<Long> getCapability() {
        return EURecipeCapability.CAP;
    }
}
