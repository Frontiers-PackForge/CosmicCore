package com.ghostipedia.cosmiccore.mixin.gttweak;

import com.ghostipedia.cosmiccore.api.machine.multiblock.ITieredMultiblockMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.tier.ElectricBlastFurnaceTierState;
import com.ghostipedia.cosmiccore.common.machine.multiblock.tier.TieredMultiblockPatterns;
import com.ghostipedia.cosmiccore.common.machine.multiblock.tier.TieredMultiblockUi;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.MultiblockWorldSavedData;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

import net.minecraft.server.level.ServerLevel;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.PanelSyncManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = CoilWorkableElectricMultiblockMachine.class, remap = false)
public abstract class ElectricBlastFurnaceTierMixin extends WorkableElectricMultiblockMachine
                                                    implements ITieredMultiblockMachine {

    protected ElectricBlastFurnaceTierMixin(BlockEntityCreationInfo info) {
        super(info);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void cosmiccore$attachTierState(BlockEntityCreationInfo info, CallbackInfo ci) {
        if (getDefinition() == GTMultiMachines.ELECTRIC_BLAST_FURNACE) {
            attachPersistentTrait("cosmiccore_ebf_tier", new ElectricBlastFurnaceTierState());
        }
    }

    @Override
    public int getStructureTier() {
        ElectricBlastFurnaceTierState state = cosmiccore$getTierState();
        return state == null ? 0 : TieredMultiblockPatterns.clampTier(getDefinition(), state.getStructureTier());
    }

    @Override
    public int getStructureTierStreak() {
        ElectricBlastFurnaceTierState state = cosmiccore$getTierState();
        return state == null ? 0 : state.getCompletedMatchingRuns();
    }

    @Override
    public boolean matchesStructureTierStreak(GTRecipe recipe) {
        ElectricBlastFurnaceTierState state = cosmiccore$getTierState();
        return state != null && recipe != null && recipe.id != null && state.matchesRecipe(recipe.id.toString());
    }

    @Override
    public void setStructureTier(int tier) {
        if (getDefinition() != GTMultiMachines.ELECTRIC_BLAST_FURNACE) return;
        int selectedTier = TieredMultiblockPatterns.clampTier(getDefinition(), tier);
        if (selectedTier == getStructureTier()) return;
        if (recipeLogic.isActive()) return;
        ElectricBlastFurnaceTierState tierState = cosmiccore$getTierState();
        if (tierState == null) return;
        if (isRemote()) {
            tierState.setClientStructureTier(selectedTier);
            return;
        }

        PatternState state = getDefaultPatternState();
        if (getLevel() instanceof ServerLevel serverLevel) {
            MultiblockWorldSavedData.getOrCreate(serverLevel).removeMapping(state);
        }
        if (isFormed()) {
            invalidateStructure();
        }
        tierState.setStructureTier(selectedTier);
        state.getCache().clear();
        state.setError(null);
        state.setShouldUpdate(true);
        state.setState(PatternState.CheckState.UNINITIALIZED);
        checkAndFormStructure();
    }

    @Override
    public void beginStructureTierRecipe(GTRecipe recipe) {
        if (getDefinition() == GTMultiMachines.ELECTRIC_BLAST_FURNACE && getStructureTier() > 0) {
            String recipeId = recipe == null || recipe.id == null ? "" : recipe.id.toString();
            ElectricBlastFurnaceTierState tierState = cosmiccore$getTierState();
            if (tierState != null) tierState.beginRecipe(recipeId);
        }
    }

    @Override
    public void completeStructureTierRecipe(GTRecipe recipe) {
        if (getDefinition() != GTMultiMachines.ELECTRIC_BLAST_FURNACE || getStructureTier() == 0) return;
        String recipeId = recipe == null || recipe.id == null ? "" : recipe.id.toString();
        ElectricBlastFurnaceTierState tierState = cosmiccore$getTierState();
        if (tierState != null) tierState.completeRecipe(recipeId);
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = super.getWidgetsForDisplay(syncManager);
        if (getDefinition() == GTMultiMachines.ELECTRIC_BLAST_FURNACE) {
            widgets.addAll(Math.min(4, widgets.size()), TieredMultiblockUi.createEbfBonusLines(
                    this::getStructureTier, this::getStructureTierStreak));
        }
        return widgets;
    }

    @Unique
    private @Nullable ElectricBlastFurnaceTierState cosmiccore$getTierState() {
        return getPersistentTrait("cosmiccore_ebf_tier");
    }
}
