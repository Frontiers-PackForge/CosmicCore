package com.ghostipedia.cosmiccore.common.machine.multiblock.electric.modular;

import net.minecraft.core.BlockPos;

public interface IMultiblockModule {
    void addBase(IModularMultiblock base);
    void removeBase(IModularMultiblock base);
    void onBaseUpdate();
    void notifyBases();
    BlockPos getPos();
}
