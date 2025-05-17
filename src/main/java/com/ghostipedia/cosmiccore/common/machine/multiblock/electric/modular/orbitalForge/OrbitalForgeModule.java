package com.ghostipedia.cosmiccore.common.machine.multiblock.electric.modular.orbitalForge;

import com.ghostipedia.cosmiccore.api.machine.multiblock.modular.WorkableElectricMultiblockMachineModule;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

public class OrbitalForgeModule extends WorkableElectricMultiblockMachineModule {
    public OrbitalForgeModule(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    public ICoilType getCoilType() {
        for (var base : getBaseMultiBlocks()) {
            if (base instanceof OrbitalForgeModularMultiblockMachine orbital) {
                return orbital.getCoilType();
            }
        }
        return null;
    }
}
