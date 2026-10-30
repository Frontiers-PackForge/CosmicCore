package com.ghostipedia.cosmiccore.utils;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

import org.jetbrains.annotations.Nullable;

public final class MachineLookUpUtils {

    private MachineLookUpUtils() {}

    public static @Nullable MetaMachine getLoadedMachine(ServerLevel level, GlobalPos position) {
        if (!position.dimension().equals(level.dimension())) return null;
        return getLoadedMachine(level, position.pos());
    }

    public static @Nullable MetaMachine getLoadedMachine(ServerLevel level, BlockPos position) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(position.getX() >> 4, position.getZ() >> 4);
        if (chunk == null) return null;
        return chunk.getBlockEntities().get(position) instanceof MetaMachine machine && !machine.isRemoved() ?
                machine : null;
    }
}
