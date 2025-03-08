package com.ghostipedia.cosmiccore.common.machine.multiblock;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

public class FloralPropagatorMachine extends WorkableElectricMultiblockMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            FloralPropagatorMachine.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);
    public FloralPropagatorMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }
}
