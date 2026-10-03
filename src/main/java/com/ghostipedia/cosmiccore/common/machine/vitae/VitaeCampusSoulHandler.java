package com.ghostipedia.cosmiccore.common.machine.vitae;

import com.ghostipedia.cosmiccore.api.capability.souls.SoulType;
import com.ghostipedia.cosmiccore.api.data.souls.SoulNetworkAccess;
import com.ghostipedia.cosmiccore.api.machine.trait.NotifiableSoulContainer;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulIngredient;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulStack;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.ImbumentPylonMachine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.server.level.ServerLevel;

import org.jetbrains.annotations.Nullable;

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
        return pylon.campusAccess().map(VitaeCampusSavedData.Access::resourceLimit).orElse(0);
    }

    @Override
    public int getCapacity(SoulType type) {
        return getThroughput(type);
    }

    @Override
    public List<SoulIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<SoulIngredient> left,
                                                  boolean simulate) {
        if (!VitaeCampusSavedData.withinPerCraftLimit(getThroughput(SoulType.Anima),
                left.stream().map(SoulIngredient::stack).toList()))
            return left;
        return super.handleRecipeInner(io, recipe, left, simulate);
    }

    @Override
    protected @Nullable SoulNetworkAccess getSoulNetwork() {
        if (!(pylon.getLevel() instanceof ServerLevel level)) return null;
        var core = pylon.campusCore();
        return core == null ? null : SoulNetworkAccess.get(level, core.resolveCampusOwner(), core.getOwnerUUID());
    }

    @Override
    public List<SoulStack> getStacks() {
        return super.getStacks().stream()
                .filter(stack -> stack.type() == SoulType.Anima || stack.type() == SoulType.Spiritus).toList();
    }

    @Override
    public int getAmount(SoulType type) {
        return type == SoulType.Anima || type == SoulType.Spiritus ? super.getAmount(type) : 0;
    }
}
