package com.ghostipedia.cosmiccore.api.machine.trait;

import com.ghostipedia.cosmiccore.api.capability.recipe.CosmicRecipeCapabilities;
import com.ghostipedia.cosmiccore.api.capability.souls.SoulType;
import com.ghostipedia.cosmiccore.api.data.souls.SoulNetwork;
import com.ghostipedia.cosmiccore.api.data.souls.SoulNetworkSavedData;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulIngredient;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulStack;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;

import net.minecraft.server.level.ServerLevel;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.function.ToIntFunction;

public class NotifiableSoulContainer extends NotifiableRecipeHandlerTrait<SoulIngredient> {

    public final IO handlerIO;

    private final ToIntFunction<SoulType> throughput;

    private final ToIntFunction<SoulType> capacity;

    public NotifiableSoulContainer(MetaMachine machine, IO io, int throughput, int capacity) {
        this(machine, io, ignored -> throughput, ignored -> capacity);
    }

    public NotifiableSoulContainer(MetaMachine machine, IO io,
                                   ToIntFunction<SoulType> throughput, ToIntFunction<SoulType> capacity) {
        super();
        this.handlerIO = io;
        this.throughput = throughput;
        this.capacity = capacity;
        machine.attachTrait(this);
    }

    @Override
    public IO getHandlerIO() {
        return handlerIO;
    }

    public int getThroughput(SoulType type) {
        return throughput.applyAsInt(type);
    }

    public int getCapacity(SoulType type) {
        return capacity.applyAsInt(type);
    }

    private SoulNetwork getSoulNetwork() {
        if (getMachine().getLevel() instanceof ServerLevel serverLevel) {
            return SoulNetworkSavedData.getSoulNetwork(serverLevel, getOwner());
        }
        return new SoulNetwork();
    }

    private UUID getOwner() {
        if (getMachine().getOwner() instanceof FTBOwner ftbOwner) {
            var team = ftbOwner.getPlayerTeam(getMachine().getOwnerUUID());
            if (team != null) return team.getTeamId();
        }
        return getMachine().getOwnerUUID();
    }

    @Override
    public List<SoulIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<SoulIngredient> left, boolean simulate) {
        if (io != handlerIO) return left;
        if (io != IO.IN && io != IO.OUT) return left.isEmpty() ? null : left;

        var network = getSoulNetwork();
        var stacks = left.stream().map(SoulIngredient::stack).toList();
        var complete = io == IO.IN ?
                network.extractAll(stacks, throughput, simulate) :
                network.insertAll(stacks, throughput, capacity, simulate);
        return complete ? null : left;
    }

    @Override
    public @NotNull List<Object> getContents() {
        return getSoulNetwork().getContents().stream()
                .map(SoulIngredient::new)
                .map(Object.class::cast)
                .toList();
    }

    /** Server-only access to the underlying network's stacks for UI display. */
    public List<SoulStack> getStacks() {
        return getSoulNetwork().getContents();
    }

    public int getAmount(SoulType type) {
        return getSoulNetwork().getAmount(type);
    }

    @Override
    public int getSize() {
        return getSoulNetwork().getContents().size();
    }

    @Override
    public double getTotalContentAmount() {
        return getSoulNetwork().getContents().stream()
                .mapToInt(SoulStack::amount)
                .sum();
    }

    @Override
    public RecipeCapability<SoulIngredient> getCapability() {
        return CosmicRecipeCapabilities.SOUL;
    }
}
