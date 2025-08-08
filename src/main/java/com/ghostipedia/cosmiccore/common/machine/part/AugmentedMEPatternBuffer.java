package com.ghostipedia.cosmiccore.common.machine.part;

import com.ghostipedia.cosmiccore.mixin.accessor.GTMMEBufferAccessor;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;

public class AugmentedMEPatternBuffer extends MEPatternBufferPartMachine {

    public AugmentedMEPatternBuffer(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        GTMMEBufferAccessor.setMaxPatternCount(45);
    }
}
