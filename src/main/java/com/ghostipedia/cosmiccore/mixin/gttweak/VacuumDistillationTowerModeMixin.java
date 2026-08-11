package com.ghostipedia.cosmiccore.mixin.gttweak;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IConfiguredMultiblockMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.tier.TieredMultiblockPatterns;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.MultiblockWorldSavedData;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.DistillationTowerMachine;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.PanelSyncManager;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(value = DistillationTowerMachine.class, remap = false)
public abstract class VacuumDistillationTowerModeMixin extends WorkableElectricMultiblockMachine
                                                       implements IConfiguredMultiblockMachine {

    protected VacuumDistillationTowerModeMixin(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public int getStructureTier() {
        return TieredMultiblockPatterns.clampTier(getDefinition(), getActiveRecipeType());
    }

    @Override
    public void setStructureTier(int tier) {
        int selectedMode = TieredMultiblockPatterns.clampTier(getDefinition(), tier);
        if (selectedMode == getStructureTier() || recipeLogic.isActive()) return;
        if (isRemote()) {
            setActiveRecipeType(selectedMode);
            return;
        }

        PatternState state = getDefaultPatternState();
        if (getLevel() instanceof ServerLevel serverLevel) {
            MultiblockWorldSavedData.getOrCreate(serverLevel).removeMapping(state);
        }
        if (isFormed()) {
            invalidateStructure();
        }
        setActiveRecipeType(selectedMode);
        state.getCache().clear();
        state.setError(null);
        state.setShouldUpdate(true);
        state.setState(PatternState.CheckState.UNINITIALIZED);
        recipeLogic.updateTickSubscription();
        checkAndFormStructure();
    }

    @Override
    public void cycleActiveRecipeType() {
        setStructureTier((getStructureTier() + 1) % TieredMultiblockPatterns.tierCount(getDefinition()));
    }

    @Override
    public boolean isConfigurationSelectionLocked() {
        return recipeLogic.isActive();
    }

    @Override
    public void setPreviewStructureTier(int tier) {
        setActiveRecipeType(TieredMultiblockPatterns.clampTier(getDefinition(), tier));
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = super.getWidgetsForDisplay(syncManager);
        widgets.add(0, Text.dynamic(() -> Component.translatable(
                "cosmiccore.multiblock.distillation_tower.mode",
                Component.translatable(TieredMultiblockPatterns.label(
                        getDefinition(), getStructureTier()).nameKey()))
                .withStyle(ChatFormatting.AQUA)).asWidget());
        if (getStructureTier() == 1) {
            widgets.add(1, Text.dynamic(() -> Component.translatable(isFormed() ?
                    "cosmiccore.multiblock.distillation_tower.vacuum_ready" :
                    "cosmiccore.multiblock.distillation_tower.vacuum_required").withStyle(isFormed() ?
                            ChatFormatting.GREEN : ChatFormatting.YELLOW))
                    .asWidget());
        }
        return widgets;
    }
}
