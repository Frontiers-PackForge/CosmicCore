package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.api.capability.recipe.IHeatContainer;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;

import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import lombok.Getter;

import java.util.Map;

@Getter
public class HeatWorkableElectricMultiblockMachine extends WorkableElectricMultiblockMachine {

    @Getter
    private IHeatContainer heatContainer = null;

    public HeatWorkableElectricMultiblockMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        Map<Long, IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts()) {
            IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.BOTH);
            if (part instanceof IHeatContainer container) {
                this.heatContainer = container;
            }
        }
    }

    ;
}
