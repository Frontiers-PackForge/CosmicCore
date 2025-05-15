package com.ghostipedia.cosmiccore.common.machine.multiblock.electric.modular;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

public class ModuleTest extends WorkableElectricModuleMachine {

    public ModuleTest(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        super.setWorkingEnabled(isWorkingAllowed);
        notifyBases();
    }
}
