package com.ghostipedia.cosmiccore.common.machine.multiblock.electric.modular;

import com.ghostipedia.cosmiccore.api.machine.multiblock.modular.WorkableElectricModularMultiblockMachine;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

public class ModularMainTest extends WorkableElectricModularMultiblockMachine {

    public ModularMainTest(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        super.setWorkingEnabled(isWorkingAllowed);
        notifyModules();
    }
}
