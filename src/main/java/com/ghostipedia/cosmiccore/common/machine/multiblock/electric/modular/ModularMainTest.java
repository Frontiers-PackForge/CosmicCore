package com.ghostipedia.cosmiccore.common.machine.multiblock.electric.modular;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;

import java.util.Map;

public class ModularMainTest extends ModularWorkableElectricMultiblockMachine {

    public ModularMainTest(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        Map<Long, IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts()) {
            System.out.println(part);
        }
    }
}
