package com.ghostipedia.cosmiccore.common.machine.multiblock.electric.modular;

import com.ghostipedia.cosmiccore.api.machine.multiblock.modular.WorkableElectricMultiblockMachineModule;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

public class ModuleTest extends WorkableElectricMultiblockMachineModule {

    public ModuleTest(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        super.setWorkingEnabled(isWorkingAllowed);
        notifyBases();
    }
}
