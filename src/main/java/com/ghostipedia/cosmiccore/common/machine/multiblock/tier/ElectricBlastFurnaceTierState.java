package com.ghostipedia.cosmiccore.common.machine.multiblock.tier;

import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;

public final class ElectricBlastFurnaceTierState extends MachineTrait {

    public static final int MAX_MATCHING_RUNS = 10;
    public static final int DURATION_REDUCTION_PERCENT_PER_RUN = 5;
    public static final MachineTraitType<ElectricBlastFurnaceTierState> TYPE = new MachineTraitType<>(
            ElectricBlastFurnaceTierState.class, false);

    @SaveField(nbtKey = "structure_tier")
    @SyncToClient
    private int structureTier;
    @SaveField(nbtKey = "streak_recipe")
    private String streakRecipeId = "";
    @SaveField(nbtKey = "streak_runs")
    @SyncToClient
    private int completedMatchingRuns;

    @Override
    public MachineTraitType<ElectricBlastFurnaceTierState> getTraitType() {
        return TYPE;
    }

    public int getStructureTier() {
        return structureTier;
    }

    public int getCompletedMatchingRuns() {
        return completedMatchingRuns;
    }

    public static int durationReductionPercent(int completedRuns) {
        return Math.clamp(completedRuns, 0, MAX_MATCHING_RUNS) * DURATION_REDUCTION_PERCENT_PER_RUN;
    }

    public boolean matchesRecipe(String recipeId) {
        return !recipeId.isEmpty() && recipeId.equals(streakRecipeId);
    }

    public void setClientStructureTier(int tier) {
        structureTier = tier;
    }

    public void setStructureTier(int tier) {
        structureTier = tier;
        streakRecipeId = "";
        completedMatchingRuns = 0;
        syncState(true);
    }

    public void beginRecipe(String recipeId) {
        if (recipeId.equals(streakRecipeId)) return;
        streakRecipeId = recipeId;
        completedMatchingRuns = 0;
        syncState(false);
    }

    public void completeRecipe(String recipeId) {
        if (!matchesRecipe(recipeId)) return;
        completedMatchingRuns = Math.min(MAX_MATCHING_RUNS, completedMatchingRuns + 1);
        syncState(false);
    }

    private void syncState(boolean structureTierChanged) {
        markAsChanged();
        if (structureTierChanged) syncDataHolder.markClientSyncFieldDirty("structureTier");
        syncDataHolder.markClientSyncFieldDirty("completedMatchingRuns");
        getMachine().getSyncDataHolder().markClientSyncFieldDirty("traitHolder");
    }
}
