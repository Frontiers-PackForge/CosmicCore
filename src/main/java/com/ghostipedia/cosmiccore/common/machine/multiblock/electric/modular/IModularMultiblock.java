package com.ghostipedia.cosmiccore.common.machine.multiblock.electric.modular;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import net.minecraft.core.BlockPos;

public interface IModularMultiblock {
    void addModule(IMultiblockModule module);
    void removeModule(IMultiblockModule module);
    void onModuleUpdate();
    void notifyModules();
    BlockPos getPos();
}
