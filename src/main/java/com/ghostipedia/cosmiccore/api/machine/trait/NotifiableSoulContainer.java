package com.ghostipedia.cosmiccore.api.machine.trait;

import com.ghostipedia.cosmiccore.api.capability.recipe.CosmicRecipeCapabilities;
import com.ghostipedia.cosmiccore.api.data.souls.SoulNetwork;
import com.ghostipedia.cosmiccore.api.data.souls.SoulNetworkSavedData;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulIngredient;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulStack;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;

import net.minecraft.server.level.ServerLevel;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NotifiableSoulContainer extends NotifiableRecipeHandlerTrait<SoulIngredient> {

    public static final MachineTraitType<NotifiableSoulContainer> TYPE =
            new MachineTraitType<>(NotifiableSoulContainer.class);

    public final IO handlerIO;

    @Getter
    private final int throughput;

    @Getter
    private final int capacity;

    public NotifiableSoulContainer(MetaMachine machine, IO io, int throughput, int capacity) {
        super(machine);
        this.handlerIO = io;
        this.throughput = throughput;
        this.capacity = capacity;
    }

    @Override
    public IO getHandlerIO() {
        return handlerIO;
    }

    public int getThroughput() {
        return throughput;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public MachineTraitType<NotifiableSoulContainer> getTraitType() {
        return TYPE;
    }

    private SoulNetwork getSoulNetwork() {
        if (this.machine.getLevel() instanceof ServerLevel serverLevel) {
            return SoulNetworkSavedData.getSoulNetwork(serverLevel, getOwner());
        }
        return new SoulNetwork();
    }

    private UUID getOwner() {
        if (this.machine.getOwner() instanceof FTBOwner ftbOwner) {
            var team = ftbOwner.getPlayerTeam(this.machine.getOwnerUUID());
            if (team != null) return team.getTeamId();
        }
        return this.machine.getOwnerUUID();
    }

    @Override
    public List<SoulIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<SoulIngredient> left, boolean simulate) {
        if (io != handlerIO) return left;
        if (io != IO.IN && io != IO.OUT) return left.isEmpty() ? null : left;

        var network = getSoulNetwork();
        List<SoulIngredient> result = new ArrayList<>();

        for (SoulIngredient ingredient : left) {
            SoulStack requiredStack = ingredient.stack();
            if (requiredStack.isEmpty()) continue;

            if (io == IO.IN) {
                SoulStack consumedStack = network.syphon(requiredStack, simulate);
                if (consumedStack.amount() < requiredStack.amount()) {
                    result.add(SoulIngredient
                            .of(requiredStack.withAmount(requiredStack.amount() - consumedStack.amount())));
                }
            } else {
                SoulStack canInput = network.add(requiredStack, throughput, capacity, simulate);
                SoulStack reminder = requiredStack.withAmount(requiredStack.amount() - canInput.amount());
                if (reminder.amount() > 0) result.add(SoulIngredient.of(reminder));
            }
        }

        return result.isEmpty() ? null : result;
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
