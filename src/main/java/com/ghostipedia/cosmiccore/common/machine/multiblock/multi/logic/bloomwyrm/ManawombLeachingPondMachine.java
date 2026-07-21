package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;

public class ManawombLeachingPondMachine extends BloomwyrmUnitMachine {

    public ManawombLeachingPondMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public boolean supportsParallelControl() {
        return false;
    }
}
