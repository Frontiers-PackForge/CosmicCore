package com.ghostipedia.cosmiccore.api.machine.multiblock;

public interface IConfiguredMultiblockMachine extends ITieredMultiblockMachine {

    boolean isConfigurationSelectionLocked();

    default void setPreviewStructureTier(int tier) {}
}
