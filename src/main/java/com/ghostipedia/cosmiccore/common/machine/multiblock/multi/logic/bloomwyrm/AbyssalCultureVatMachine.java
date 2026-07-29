package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;

public class AbyssalCultureVatMachine extends BloomwyrmUnitMachine {

    public AbyssalCultureVatMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public boolean supportsParallelControl() {
        return false;
    }
}
