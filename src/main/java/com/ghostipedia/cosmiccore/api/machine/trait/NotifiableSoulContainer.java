package com.ghostipedia.cosmiccore.api.machine.trait;

import com.ghostipedia.cosmiccore.api.capability.recipe.CosmicRecipeCapabilities;
import com.ghostipedia.cosmiccore.api.data.souls.SoulNetwork;
import com.ghostipedia.cosmiccore.api.data.souls.SoulNetworkSavedData;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulIngredient;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulStack;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import lombok.Getter;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class NotifiableSoulContainer extends NotifiableRecipeHandlerTrait<SoulIngredient> {

    @Getter
    public final IO handlerIO;

    private final int throughput;

    public NotifiableSoulContainer(MetaMachine machine, IO io, int throughput) {
        super(machine);
        this.handlerIO = io;
        this.throughput = throughput;
    }

    private SoulNetwork getSoulNetwork() {
        if (this.machine.getLevel() instanceof ServerLevel serverLevel) {
            return SoulNetworkSavedData.getSoulNetwork(serverLevel, getOwner());
        }
        return new SoulNetwork();
    }

    private UUID getOwner() {
        var team = ((FTBOwner) this.machine.getOwner()).getPlayerTeam(this.machine.getOwnerUUID());
        return team != null ? team.getTeamId() : this.machine.getOwnerUUID();
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
                if (consumedStack.amount() < requiredStack.amount()){
                    result.add(SoulIngredient.of(requiredStack.withAmount(requiredStack.amount() - consumedStack.amount())));
                }
            } else {
                SoulStack canInput = network.add(requiredStack, throughput, simulate);
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
