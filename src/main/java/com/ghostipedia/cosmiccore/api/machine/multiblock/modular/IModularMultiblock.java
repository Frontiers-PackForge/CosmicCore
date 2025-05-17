package com.ghostipedia.cosmiccore.api.machine.multiblock.modular;

import net.minecraft.core.BlockPos;

public interface IModularMultiblock {

    void addModule(IMultiblockModule module);

    void removeModule(IMultiblockModule module);

    void onModuleUpdate();

    void notifyModules();

    int getModuleCount();

    boolean isWorking();

    BlockPos getPos();
}
