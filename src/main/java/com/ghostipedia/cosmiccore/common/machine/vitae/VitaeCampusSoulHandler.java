package com.ghostipedia.cosmiccore.common.machine.vitae;

import com.ghostipedia.cosmiccore.api.capability.souls.SoulType;
import com.ghostipedia.cosmiccore.api.data.souls.SoulNetwork;
import com.ghostipedia.cosmiccore.api.data.souls.SoulNetworkSavedData;
import com.ghostipedia.cosmiccore.api.machine.activity.ActivityScope;
import com.ghostipedia.cosmiccore.api.machine.trait.NotifiableSoulContainer;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulIngredient;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulStack;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.ImbumentPylonMachine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.server.level.ServerLevel;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class VitaeCampusSoulHandler extends NotifiableSoulContainer {

    private final ImbumentPylonMachine pylon;

    public VitaeCampusSoulHandler(ImbumentPylonMachine pylon) {
        super(pylon, IO.IN, 0, 0);
        this.pylon = pylon;
    }

    @Override
    public int getThroughput(SoulType type) {
        if (type != SoulType.Anima && type != SoulType.Spiritus) return 0;
        return access().map(VitaeCampusSavedData.Access::resourceLimit).orElse(0);
    }

    @Override
    public int getCapacity(SoulType type) {
        return getThroughput(type);
    }

    @Override
    public List<SoulIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<SoulIngredient> left,
                                                  boolean simulate) {
        if (io != IO.IN) return left;
        var access = access().orElse(null);
        if (access == null) return left;
        List<SoulStack> stacks = left.stream().map(SoulIngredient::stack).toList();
        if (!VitaeCampusSavedData.withinPerCraftLimit(access.resourceLimit(), stacks)) return left;
        SoulNetwork network = SoulNetworkSavedData.getSoulNetwork((ServerLevel) pylon.getLevel(), access.owner());
        int animaBefore = network.getAmount(SoulType.Anima);
        int spiritusBefore = network.getAmount(SoulType.Spiritus);
        if (!network.extractAll(stacks, ignored -> access.resourceLimit(), simulate)) return left;
        if (!simulate) {
            recordInput(SoulType.Anima, animaBefore, network.getAmount(SoulType.Anima));
            recordInput(SoulType.Spiritus, spiritusBefore, network.getAmount(SoulType.Spiritus));
        }
        left.clear();
        return left;
    }

    @Override
    public @NotNull List<Object> getContents() {
        var access = access().orElse(null);
        if (access == null) return List.of();
        return SoulNetworkSavedData.getSoulNetwork((ServerLevel) pylon.getLevel(), access.owner()).getContents()
                .stream()
                .filter(stack -> stack.type() == SoulType.Anima || stack.type() == SoulType.Spiritus)
                .map(SoulIngredient::new)
                .map(Object.class::cast)
                .toList();
    }

    @Override
    public List<SoulStack> getStacks() {
        return getContents().stream().map(SoulIngredient.class::cast).map(SoulIngredient::stack).toList();
    }

    @Override
    public int getAmount(SoulType type) {
        var access = access().orElse(null);
        if (access == null || (type != SoulType.Anima && type != SoulType.Spiritus)) return 0;
        return SoulNetworkSavedData.getSoulNetwork((ServerLevel) pylon.getLevel(), access.owner()).getAmount(type);
    }

    @Override
    public int getSize() {
        return getContents().size();
    }

    @Override
    public double getTotalContentAmount() {
        return getStacks().stream().mapToInt(SoulStack::amount).sum();
    }

    private java.util.Optional<VitaeCampusSavedData.Access> access() {
        if (!pylon.isFormed() || !(pylon.getLevel() instanceof ServerLevel level)) return java.util.Optional.empty();
        return VitaeCampusSavedData.get(level.getServer()).accessFor(pylon.globalPosition());
    }

    private static void recordInput(SoulType type, int before, int after) {
        if (after < before) ActivityScope.soul(type, (long) before - after, true);
    }
}
