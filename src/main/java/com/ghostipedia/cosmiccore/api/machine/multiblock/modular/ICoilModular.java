package com.ghostipedia.cosmiccore.api.machine.multiblock.modular;

import com.gregtechceu.gtceu.api.block.ICoilType;

public interface ICoilModular {
    int getTier();
    int getCoilTier();
    ICoilType getCoilType();
    long getOverclockVoltage();
}
