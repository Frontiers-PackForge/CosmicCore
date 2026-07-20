package com.ghostipedia.cosmiccore.common.compat.create;

import net.minecraft.core.BlockPos;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import org.apache.commons.lang3.tuple.Pair;

public interface MovingDeployerContext {

    MovementContext cosmiccore$getMovementContext();

    void cosmiccore$setMovementContext(MovementContext context);

    Pair<BlockPos, Float> cosmiccore$getBlockBreakingProgress();

    void cosmiccore$setBlockBreakingProgress(Pair<BlockPos, Float> progress);
}
