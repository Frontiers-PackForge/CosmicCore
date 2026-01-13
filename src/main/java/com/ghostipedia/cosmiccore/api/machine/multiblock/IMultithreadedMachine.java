package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.MultithreadedRecipeLogic;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

/**
 * Interface for machines that can run multiple independent recipe threads.
 */
public interface IMultithreadedMachine {

    /**
     * Get the map of thread color to recipe logic.
     */
    Int2ObjectMap<MultithreadedRecipeLogic> getThreadLogicsMap();

    /**
     * Get the maximum number of threads this machine can support.
     * Determined by energy hatch amperage.
     */
    int getMaxThreadCount();

    /**
     * Get the current number of configured threads.
     * Limited by available color-coded input buses.
     */
    int getCurrentThreadCount();

    /**
     * Get the number of threads currently running recipes.
     */
    int getRunningThreadCount();
}
