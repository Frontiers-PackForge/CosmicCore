package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.api.capability.recipe.IHeatContainer;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;

import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import lombok.Getter;

import java.util.Map;

@Getter
public class HeatWorkableElectricMultiblockMachine extends WorkableElectricMultiblockMachine {

    @Getter
    private IHeatContainer heatContainer = null;

    public HeatWorkableElectricMultiblockMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public void formStructure(@org.jetbrains.annotations.NotNull String substructureName) {
        super.formStructure(substructureName);
        for (IMultiPart part : getParts()) {
            if (part instanceof IHeatContainer container) {
                this.heatContainer = container;
            }
        }
    }
}
