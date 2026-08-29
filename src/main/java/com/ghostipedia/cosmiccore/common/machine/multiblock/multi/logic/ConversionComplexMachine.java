package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IConfiguredMultiblockMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.tier.TieredMultiblockPatterns;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.MultiblockWorldSavedData;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.PanelSyncManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConversionComplexMachine extends WorkableElectricMultiblockMachine
                                      implements IConfiguredMultiblockMachine {

    public static final IntegerProperty CONFIGURATION_PROPERTY = IntegerProperty.create("configuration", 0, 5);

    @SaveField(nbtKey = "configuration")
    @SyncToClient
    private int configuration;

    public ConversionComplexMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        applyConfiguration(TieredMultiblockPatterns.clampTier(getDefinition(), configuration));
    }

    @Override
    public int getStructureTier() {
        return TieredMultiblockPatterns.clampTier(getDefinition(), configuration);
    }

    @Override
    public void setStructureTier(int tier) {
        int selectedConfiguration = TieredMultiblockPatterns.clampTier(getDefinition(), tier);
        if (selectedConfiguration == getStructureTier() || recipeLogic.isActive() ||
                isConfigurationSelectionLocked())
            return;
        if (isRemote()) {
            applyConfiguration(selectedConfiguration);
            return;
        }

        PatternState state = getDefaultPatternState();
        if (getLevel() instanceof ServerLevel serverLevel) {
            MultiblockWorldSavedData.getOrCreate(serverLevel).removeMapping(state);
        }
        if (isFormed()) {
            invalidateStructure();
        }
        applyConfiguration(selectedConfiguration);
        getSyncDataHolder().markClientSyncFieldDirty("configuration");
        state.getCache().clear();
        state.clearErrors();
        state.setShouldUpdate(true);
        state.setState(PatternState.CheckState.UNINITIALIZED);
        recipeLogic.updateTickSubscription();
        checkAndFormStructure();
    }

    @Override
    public void cycleActiveRecipeType() {
        int nextConfiguration = getStructureTier() + 1;
        if (nextConfiguration >= TieredMultiblockPatterns.tierCount(getDefinition())) {
            nextConfiguration = 1;
        }
        setStructureTier(nextConfiguration);
    }

    @Override
    public boolean isConfigurationSelectionLocked() {
        return isFormed() && getStructureTier() != 0;
    }

    @Override
    protected InteractionResult onScrewdriverClick(ExtendedUseOnContext context) {
        var player = context.getPlayer();
        if (!player.isShiftKeyDown() || getCoverContainer().hasCover(context.getGridSide())) {
            return super.onScrewdriverClick(context);
        }
        if (isConfigurationSelectionLocked()) {
            if (!isRemote()) {
                player.displayClientMessage(
                        Component.translatable("cosmiccore.multiblock.configuration.physically_locked"), true);
            }
            return InteractionResult.SUCCESS;
        }
        if (!isRemote()) {
            int nextConfiguration = (getStructureTier() + 1) %
                    TieredMultiblockPatterns.tierCount(getDefinition());
            setStructureTier(nextConfiguration);
            player.displayClientMessage(Component.translatable("cosmiccore.multiblock.construction_blueprint",
                    Component.translatable(
                            TieredMultiblockPatterns.label(getDefinition(), getStructureTier()).nameKey())),
                    true);
        }
        return InteractionResult.sidedSuccess(isRemote());
    }

    @Override
    public void setPreviewStructureTier(int tier) {
        int selectedConfiguration = TieredMultiblockPatterns.clampTier(getDefinition(), tier);
        setConfigurationRenderState(selectedConfiguration);
    }

    @Override
    protected @Nullable GTRecipe getRealRecipe(GTRecipe recipe) {
        int selectedConfiguration = getStructureTier();
        if (selectedConfiguration == 0 ||
                recipe.recipeType != getRecipeTypes()[recipeTypeIndex(selectedConfiguration)]) {
            return null;
        }
        return super.getRealRecipe(recipe);
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = super.getWidgetsForDisplay(syncManager);
        widgets.add(0, Text.dynamic(() -> Component.translatable(isFormed() ?
                "cosmiccore.multiblock.detected_configuration" :
                "cosmiccore.multiblock.construction_blueprint",
                Component.translatable(TieredMultiblockPatterns.label(getDefinition(), getStructureTier()).nameKey()))
                .withStyle(ChatFormatting.AQUA)).asWidget());
        widgets.add(1, Text.dynamic(() -> isFormed() && getStructureTier() == 0 ?
                Component.translatable("cosmiccore.multiblock.configuration.core_only")
                        .withStyle(ChatFormatting.YELLOW) :
                Component.empty()).asWidget());
        return widgets;
    }

    private int recipeTypeIndex(int selectedConfiguration) {
        return Math.max(0, selectedConfiguration - 1);
    }

    private void applyConfiguration(int selectedConfiguration) {
        configuration = selectedConfiguration;
        setActiveRecipeType(recipeTypeIndex(selectedConfiguration));
        setConfigurationRenderState(selectedConfiguration);
    }

    private void setConfigurationRenderState(int selectedConfiguration) {
        var renderState = getRenderState();
        if (renderState.hasProperty(CONFIGURATION_PROPERTY) &&
                renderState.getValue(CONFIGURATION_PROPERTY) != selectedConfiguration) {
            setRenderState(renderState.setValue(CONFIGURATION_PROPERTY, selectedConfiguration));
        }
    }
}
