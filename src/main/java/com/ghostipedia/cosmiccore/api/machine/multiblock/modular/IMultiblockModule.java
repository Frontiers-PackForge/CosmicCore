package com.ghostipedia.cosmiccore.api.machine.multiblock.modular;

import net.minecraft.core.BlockPos;

public interface IMultiblockModule {

    void addBase(IModularMultiblock base);

    void removeBase(IModularMultiblock base);

    void onBaseUpdate();

    void notifyBases();

    int getBaseCount();

    BlockPos getPos();
}
