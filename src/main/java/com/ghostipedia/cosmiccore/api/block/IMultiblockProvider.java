package com.ghostipedia.cosmiccore.api.block;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;

public interface IMultiblockProvider {

    int getModulatorTier();

    IEnergyContainer getEnergyContainersForModules();

    boolean amIAModule(IMultiblockProvider receiver);
}
