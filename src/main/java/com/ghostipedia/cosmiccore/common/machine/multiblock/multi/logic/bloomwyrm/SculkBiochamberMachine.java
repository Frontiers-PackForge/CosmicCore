package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;

public class SculkBiochamberMachine extends BloomwyrmUnitMachine {

    public SculkBiochamberMachine(BlockEntityCreationInfo info) {
        super(info, new IndependentBloomwyrmRecipeLogic());
        getRecipeLogic().setKeepSubscribing(true);
    }

    @Override
    public boolean usesHeartCycleAllocation() {
        return false;
    }
}
