package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;

import net.minecraft.network.chat.Component;

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

    public float getRemovalChance() {
        return switch (tier) {
            case GTValues.LV -> 0.50f;
            case GTValues.HV -> 0.25f;
            case GTValues.IV -> 0.125f;
            case GTValues.ZPM -> 0.0625f;
            default -> 0.50f;
        };
    }

    public int getEffectiveYieldMultiplier() {
        return Math.round(1.0f / getRemovalChance());
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

    public long getEnergyPerTick() {
        return GTValues.V[tier];
    }

    public boolean drainEnergy(boolean simulate) {
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
    protected net.minecraft.world.InteractionResult onScrewdriverClick(
                                                                       com.gregtechceu.gtceu.utils.ExtendedUseOnContext context) {
        if (!isRemote()) {
            getRecipeLogic().restartDrill();
            context.getPlayer().sendSystemMessage(
                    Component.translatable("cosmiccore.machine.ore_extraction_drill.restarted"));
        }
        return net.minecraft.world.InteractionResult.sidedSuccess(isRemote());
    }

    // TODO(8.0.0 MUI2): createUIWidget/createUI/addDisplayText (LDLib UI: drill-phase + ore-scan status via
    // OreExtractionDrillWidget) removed in GTCEu 8.0.0. Rebuild on MUI2; OreExtractionDrillLogic supplies the data.
}
