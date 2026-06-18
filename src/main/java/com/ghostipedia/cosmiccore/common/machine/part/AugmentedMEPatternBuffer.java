package com.ghostipedia.cosmiccore.common.machine.part;

import com.ghostipedia.cosmiccore.mixin.accessor.GTMMEBufferAccessor;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;

public class AugmentedMEPatternBuffer extends MEPatternBufferPartMachine {

    public AugmentedMEPatternBuffer(BlockEntityCreationInfo holder, Object... args) {
        super(holder, args);
        GTMMEBufferAccessor.setMaxPatternCount(45);
    }
}
