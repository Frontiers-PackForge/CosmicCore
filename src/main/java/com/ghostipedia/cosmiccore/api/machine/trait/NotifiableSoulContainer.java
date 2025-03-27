package com.ghostipedia.cosmiccore.api.machine.trait;

import com.ghostipedia.cosmiccore.api.capability.ISoulContainer;
import com.ghostipedia.cosmiccore.api.capability.recipe.SoulRecipeCapability;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class NotifiableSoulContainer extends NotifiableRecipeHandlerTrait<Integer> implements ISoulContainer {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(NotifiableSoulContainer.class,
            NotifiableRecipeHandlerTrait.MANAGED_FIELD_HOLDER);

    @Getter
    private final IO handlerIO;
    private final ConditionalSubscriptionHandler conditionalSubscriptionHandler;

    @Getter
    @Persisted
    @DescSynced
    private UUID owner;

    @Getter
    @DescSynced
    private int currentEssence;

    @Persisted
    private int maxCapacity;

    @Persisted
    private int maxConsumption;

    public NotifiableSoulContainer(MetaMachine machine, IO io, int maxCapacity, int maxConsumption) {
        super(machine);
        this.handlerIO = io;
        this.currentEssence = -1;
        this.maxCapacity = maxCapacity;
        this.maxConsumption = maxConsumption;
        conditionalSubscriptionHandler = new ConditionalSubscriptionHandler(machine, this::querySoulNetwork,
                () -> owner != null);
    }

    private void querySoulNetwork() {
        if (this.machine.getOffsetTimer() % 20 != 0) return;

        var network = this.getSoulNetwork();
        if (network == null) return;

        var essence = network.getCurrentEssence();
        if (this.currentEssence == essence) return;

        this.currentEssence = essence;
        this.notifyListeners();
    }

    @Override
    public List<Integer> handleRecipeInner(IO io, GTRecipe recipe, List<Integer> left, @Nullable String slotName,
                                           boolean simulate) {
        ISoulContainer container = this;
        if (container.getOwner() == null) return null;

        int lifeEssence = left.stream().reduce(0, Integer::sum);
        if (io == IO.IN) {
            var canOutput = Math.min(this.maxConsumption, container.getSoulNetwork().getCurrentEssence());
            if (!simulate) lifeEssence = container.getSoulNetwork().syphon(
                    SoulTicket.block(this.machine.getLevel(), this.machine.getPos(), Math.min(canOutput, lifeEssence)),
                    false);
            lifeEssence = lifeEssence - canOutput;
        } else if (io == IO.OUT) {
            var canInput = this.maxCapacity - container.getSoulNetwork().getCurrentEssence();
            if (!simulate) lifeEssence = container.getSoulNetwork().add(
                    SoulTicket.block(this.machine.getLevel(), this.machine.getPos(), Math.min(canInput, lifeEssence)),
                    this.maxCapacity);
            lifeEssence = lifeEssence - canInput;
        }

        return lifeEssence <= 0 ? null : Collections.singletonList(lifeEssence);
    }

    @Override
    public List<Object> getContents() {
        if (this.owner == null) return Collections.emptyList();
        return List.of(this.getSoulNetwork().getCurrentEssence());
    }

    @Override
    public double getTotalContentAmount() {
        if (this.owner == null) return 0;
        return this.getSoulNetwork().getCurrentEssence();
    }

    @Override
    public RecipeCapability<Integer> getCapability() {
        return SoulRecipeCapability.CAP;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public SoulNetwork getSoulNetwork() {
        return NetworkHelper.getSoulNetwork(this.owner);
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public void setOwner(UUID owner) {
        this.owner = owner;
        conditionalSubscriptionHandler.updateSubscription();
    }

    @Override
    public void onMachineLoad() {
        super.onMachineLoad();
        conditionalSubscriptionHandler.initialize(this.machine.getLevel());
    }
}
