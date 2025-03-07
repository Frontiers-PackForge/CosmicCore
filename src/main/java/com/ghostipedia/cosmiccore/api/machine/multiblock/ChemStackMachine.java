package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;

public class ChemStackMachine implements IDisplayUIMachine {

    public static final int MAX_PARALLELS = 8;
    private static final double CONVERSION_RATE = 0.5D;

    @Override
    public boolean isFormed() {
        return false;
    }

    @Override
    public @NotNull MultiblockState getMultiblockState() {
        return null;
    }

    @Override
    public void asyncCheckPattern(long periodID) {}

    @Override
    public void onStructureFormed() {}

    @Override
    public void onStructureInvalid() {}

    @Override
    public boolean hasFrontFacing() {
        return false;
    }

    @Override
    public List<IMultiPart> getParts() {
        return null;
    }

    @Override
    public Optional<IParallelHatch> getParallelHatch() {
        return Optional.empty();
    }

    @Override
    public void onPartUnload() {}

    @Override
    public Lock getPatternLock() {
        return null;
    }
}
