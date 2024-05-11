package com.ghostipedia.cosmiccore.api.machine.trait;

import com.ghostipedia.cosmiccore.api.capability.recipe.SoulRecipeCapability;
import com.ghostipedia.cosmiccore.api.capability.ISoulContainer;
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
import com.gregtechceu.gtceu.api.GTValues;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class NotifiableSoulContainer extends NotifiableRecipeHandlerTrait<Integer> implements ISoulContainer {
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(NotifiableSoulContainer.class, NotifiableRecipeHandlerTrait.MANAGED_FIELD_HOLDER);

    @Getter
    private final IO handlerIO;
    private final ConditionalSubscriptionHandler conditionalSubscriptionHandler;

    @Getter
    @Persisted @DescSynced
    private UUID owner;

    @Getter
    @DescSynced
    private int currentEssence;

    @Persisted
    private int tier;

    public NotifiableSoulContainer(MetaMachine machine, IO io, int tier) {
        super(machine);
        this.handlerIO = io;
        this.currentEssence = -1;
        this.tier = tier;
        conditionalSubscriptionHandler = new ConditionalSubscriptionHandler(machine, this::querySoulNetwork, () -> owner != null);
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

    private int getMaxCapacity(int tier) {
        return switch (tier) {
            case GTValues.ZPM -> 10000000;
            case GTValues.UV  -> 20000000;
            case GTValues.UHV -> 50000000;
            case GTValues.UEV -> 100000000;
            case GTValues.UIV -> 250000000;
            case GTValues.UXV -> 500000000;
            case GTValues.OpV -> 1000000000;
            case GTValues.MAX -> Integer.MAX_VALUE;
            default -> 0;
        };
    }

    private int getMaxConsumption(int tier) {
        return switch (tier) {
            case GTValues.ZPM -> 5000000;
            case GTValues.UV  -> 10000000;
            case GTValues.UHV -> 25000000;
            case GTValues.UEV -> 50000000;
            case GTValues.UIV -> 125000000;
            case GTValues.UXV -> 250000000;
            case GTValues.OpV -> 500000000;
            case GTValues.MAX -> Integer.MAX_VALUE;
            default -> 0;
        };
    }

    @Override
    public List<Integer> handleRecipeInner(IO io, GTRecipe recipe, List<Integer> left, @Nullable String slotName, boolean simulate) {
        ISoulContainer container = this;
        if (container.getOwner() == null) return null;

        int lifeEssence = left.stream().reduce(0, Integer::sum);
        if (io == IO.IN) {
            var canOutput = Math.min(this.getMaxConsumption(this.tier), container.getSoulNetwork().getCurrentEssence());
            if (!simulate) lifeEssence = container.getSoulNetwork().syphon(SoulTicket.block(this.machine.getLevel(), this.machine.getPos(), Math.min(canOutput, lifeEssence)), false);
            lifeEssence = lifeEssence - canOutput;
        } else if (io == IO.OUT) {
            var canInput = this.getMaxCapacity(this.tier) - container.getSoulNetwork().getCurrentEssence();
            if (!simulate) lifeEssence = container.getSoulNetwork().add(SoulTicket.block(this.machine.getLevel(), this.machine.getPos(), Math.min(canInput, lifeEssence)), this.getMaxCapacity(tier));
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
        if (this.owner == null)return 0;
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
