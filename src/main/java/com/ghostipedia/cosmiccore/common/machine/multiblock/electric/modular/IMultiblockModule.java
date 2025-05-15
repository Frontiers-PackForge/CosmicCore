package com.ghostipedia.cosmiccore.common.machine.multiblock.electric.modular;

import net.minecraft.core.BlockPos;

public interface IMultiblockModule {
    void onMultiblockUpdate();
    BlockPos getPos();
}
