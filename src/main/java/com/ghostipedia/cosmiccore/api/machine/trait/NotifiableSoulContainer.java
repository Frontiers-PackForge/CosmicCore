package com.ghostipedia.cosmiccore.api.machine.trait;

import com.ghostipedia.cosmiccore.api.capability.ISoulContainer;
import com.ghostipedia.cosmiccore.api.capability.recipe.SoulRecipeCapability;

import com.ghostipedia.cosmiccore.api.data.souls.SoulNetwork;
import com.ghostipedia.cosmiccore.api.data.souls.SoulNetworkSavedData;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import lombok.Getter;

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
        // TODO: simplify to remove conditional that is not needed
        conditionalSubscriptionHandler = new ConditionalSubscriptionHandler(machine, this::querySoulNetwork, () -> true);
    }

    private void querySoulNetwork() {
        if (this.machine.getOffsetTimer() % 20 != 0) return;

        var network = this.getSoulNetwork();
        if (network == null) return;

        var essence = network.getCurrentSouls();
        if (this.currentEssence == essence) return;

        this.currentEssence = essence;
        this.notifyListeners();
    }

    @Override
    public List<Integer> handleRecipeInner(IO io, GTRecipe recipe, List<Integer> left, boolean simulate) {
        ISoulContainer container = this;

        int lifeEssence = left.stream().reduce(0, Integer::sum);
        if (io == IO.IN) {
            var canOutput = Math.min(this.maxConsumption, container.getSoulNetwork().getCurrentSouls());
            if (!simulate) lifeEssence = container.getSoulNetwork().syphon(Math.min(canOutput, lifeEssence));
            lifeEssence = lifeEssence - canOutput;
        } else if (io == IO.OUT) {
            var canInput = this.maxCapacity - container.getSoulNetwork().getCurrentSouls();
            if (!simulate) lifeEssence = container.getSoulNetwork().add(Math.min(canInput, lifeEssence), this.maxCapacity);
            lifeEssence = lifeEssence - canInput;
        }

        return lifeEssence <= 0 ? null : Collections.singletonList(lifeEssence);
    }

    @Override
    public List<Object> getContents() {
        return List.of(this.getSoulNetwork().getCurrentSouls());
    }

    @Override
    public double getTotalContentAmount() {
        return this.getSoulNetwork().getCurrentSouls();
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
        return SoulNetworkSavedData.getSoulNetwork(getOwner());
    }

    public UUID getOwner() {
        var team = ((FTBOwner) this.machine.getOwner()).getPlayerTeam(this.machine.getOwnerUUID());
        return team != null ? team.getTeamId() : this.machine.getOwnerUUID();
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public void onMachineLoad() {
        super.onMachineLoad();
        conditionalSubscriptionHandler.initialize(this.machine.getLevel());
    }
}
