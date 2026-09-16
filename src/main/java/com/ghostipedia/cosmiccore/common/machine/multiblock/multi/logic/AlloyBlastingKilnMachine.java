package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.machine.foundry.AlloyBlastingKilnStructure;
import com.ghostipedia.cosmiccore.common.machine.foundry.FoundryCampusSavedData;
import com.ghostipedia.cosmiccore.common.machine.foundry.FoundryCampusSync;
import com.ghostipedia.cosmiccore.common.machine.foundry.FoundryFurnaceEndpoint;
import com.ghostipedia.cosmiccore.common.machine.foundry.FoundryRenderAnchor;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;

import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class AlloyBlastingKilnMachine extends WorkableMultiblockMachine implements FoundryFurnaceEndpoint {

    public static final int MAX_PARALLEL = 4;
    private static final UUID UNOWNED = new UUID(0, 0);
    private boolean unloading;

    public AlloyBlastingKilnMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public void onLoad() {
        unloading = false;
        super.onLoad();
    }

    @Override
    public void onUnload() {
        unloading = true;
        notifyCampus();
        super.onUnload();
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        notifyCampus();
    }

    @Override
    public void invalidateStructure(String substructureName) {
        super.invalidateStructure(substructureName);
        notifyCampus();
    }

    @Override
    public void onMachineDestroyed() {
        unloading = true;
        notifyCampus();
        super.onMachineDestroyed();
    }

    @Override
    public UUID foundryOwner() {
        UUID owner = getOwnerUUID();
        if (owner == null) return UNOWNED;
        if (getOwner() instanceof FTBOwner ftbOwner) {
            var team = ftbOwner.getPlayerTeam(owner);
            if (team != null) return team.getTeamId();
        }
        return owner;
    }

    @Override
    public ResourceLocation foundryFurnaceType() {
        return CosmicCore.id("alloy_blasting_kiln");
    }

    @Override
    public FoundryRenderAnchor foundryReceivingAnchor() {
        return AlloyBlastingKilnStructure.receivingAnchor();
    }

    @Override
    public boolean isFoundryStructureFormed() {
        return isFormed() && !unloading;
    }

    @Override
    public boolean isFoundryWorking() {
        return getRecipeLogic().isWorking();
    }

    public boolean canRunFoundryRecipe() {
        if (!(getLevel() instanceof ServerLevel level)) return false;
        return campusResourceContractReady() && FoundryCampusSavedData.get(level.getServer())
                .canOperate(globalPosition(), this);
    }

    public static int boundedParallel(int availableParallel) {
        return Math.max(0, Math.min(MAX_PARALLEL, availableParallel));
    }

    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof AlloyBlastingKilnMachine kiln)) {
            return com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier
                    .nullWrongType(AlloyBlastingKilnMachine.class, machine);
        }
        if (!kiln.canRunFoundryRecipe()) return ModifierFunction.NULL;
        int parallel = boundedParallel(ParallelLogic.getParallelAmountWithoutEU(kiln, recipe, MAX_PARALLEL));
        if (parallel < 1) return ModifierFunction.NULL;
        if (parallel == 1) return ModifierFunction.IDENTITY;
        return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(parallel))
                .parallels(parallel)
                .build();
    }

    private boolean campusResourceContractReady() {
        return false;
    }

    private void notifyCampus() {
        if (!(getLevel() instanceof ServerLevel level)) return;
        FoundryCampusSavedData.get(level.getServer()).coreFor(globalPosition())
                .ifPresent(core -> FoundryCampusSync.send(level, core));
    }

    private GlobalPos globalPosition() {
        return GlobalPos.of(getLevel().dimension(), getBlockPos());
    }
}
