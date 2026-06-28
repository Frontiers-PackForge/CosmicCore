package com.ghostipedia.cosmiccore.integration.gtceu;

import com.gregtechceu.gtceu.api.blockentity.IGregtechBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class GregtechUnloadRenderGuard {

    private GregtechUnloadRenderGuard() {}

    public static boolean wouldBlockOnChunk(IGregtechBlockEntity be) {
        Level level = be.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            BlockPos pos = be.getBlockPos();
            return serverLevel.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) == null;
        }
        return false;
    }
}
