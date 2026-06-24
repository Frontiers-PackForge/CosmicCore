package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.MultithreadedMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.MultithreadedRecipeLogic;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;

/**
 * The Dreamer's Basin Machine - A multithreaded processing machine.
 * <p>
 * TODO(8.0.0 MUI2): custom display text shelved; base default getWidgetsForDisplay UI used for now (original
 * thread-status / per-thread progress / recipe-tooltip display text is preserved in git history).
 */
public class DreamersBasinMachine extends MultithreadedMachine {

    public DreamersBasinMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    /**
     * Get all thread logics for iteration.
     */
    public Iterable<MultithreadedRecipeLogic> getThreadLogicsIterable() {
        return getThreadLogics().values();
    }
}
