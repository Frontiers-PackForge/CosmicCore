package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.ui.OreExtractionDrillUI;
import com.ghostipedia.cosmiccore.common.murkbloom.AbyssMachineRestrictions;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;

import net.minecraft.network.chat.Component;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class OreExtractionDrillMachine extends WorkableElectricMultiblockMachine {

    @Getter
    private final int tier;

    public OreExtractionDrillMachine(BlockEntityCreationInfo holder, int tier) {
        super(holder, new OreExtractionDrillLogic());
        this.tier = tier;
    }

    @NotNull
    @Override
    public OreExtractionDrillLogic getRecipeLogic() {
        return (OreExtractionDrillLogic) super.getRecipeLogic();
    }

    public int getTierIndex() {
        return switch (tier) {
            case GTValues.LV -> 0;
            case GTValues.HV -> 1;
            case GTValues.IV -> 2;
            case GTValues.ZPM -> 3;
            default -> 0;
        };
    }

    public int getChunkDiameter() {
        return getChunkDiameterForTier(tier);
    }

    public static int getChunkDiameterForTier(int tier) {
        return switch (tier) {
            case GTValues.HV -> 11;
            case GTValues.IV -> 13;
            case GTValues.ZPM -> 15;
            default -> 9;
        };
    }

    public long getEnergyPerTick() {
        return GTValues.V[tier];
    }

    public boolean drainEnergy(boolean simulate) {
        if (AbyssMachineRestrictions.inUndergarden(getLevel())) return false;
        var energyHandlers = this.getCapabilitiesFlat(IO.IN, EURecipeCapability.CAP);
        if (energyHandlers == null) return false;

        var energyList = new EnergyContainerList(
                energyHandlers.stream()
                        .filter(IEnergyContainer.class::isInstance)
                        .map(IEnergyContainer.class::cast)
                        .toList());

        long toDrain = getEnergyPerTick();
        if (energyList.getEnergyStored() < toDrain) {
            return false;
        }

        if (!simulate) {
            energyList.removeEnergy(toDrain);
        }
        return true;
    }

    @Override
    public void formStructure(@org.jetbrains.annotations.NotNull String substructureName) {
        super.formStructure(substructureName);
        getRecipeLogic().invalidateCache();
        getRecipeLogic().loadStructureChunks();
    }

    @Override
    public void invalidateStructure(String substructureName) {
        super.invalidateStructure(substructureName);
        getRecipeLogic().invalidateCache();
        getRecipeLogic().releaseAllChunks();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        getRecipeLogic().releaseAllChunks();
    }

    @Override
    public ModularPanel<?> buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return OreExtractionDrillUI.build(this, data, syncManager, settings);
    }

    @Override
    protected net.minecraft.world.InteractionResult onScrewdriverClick(
                                                                       com.gregtechceu.gtceu.utils.ExtendedUseOnContext context) {
        if (!isRemote()) {
            getRecipeLogic().restartDrill();
            context.getPlayer().sendSystemMessage(
                    Component.translatable("cosmiccore.machine.ore_extraction_drill.restarted"));
        }
        return net.minecraft.world.InteractionResult.sidedSuccess(isRemote());
    }
}
