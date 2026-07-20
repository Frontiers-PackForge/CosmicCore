package com.ghostipedia.cosmiccore.mixin.create;

import com.ghostipedia.cosmiccore.common.compat.create.MovingDeployerContext;

import net.minecraft.core.BlockPos;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DeployerFakePlayer.class)
public abstract class DeployerFakePlayerMixin implements MovingDeployerContext {

    @Shadow
    Pair<BlockPos, Float> blockBreakingProgress;

    @Unique
    private MovementContext cosmiccore$movementContext;

    @Override
    public MovementContext cosmiccore$getMovementContext() {
        return cosmiccore$movementContext;
    }

    @Override
    public void cosmiccore$setMovementContext(MovementContext context) {
        cosmiccore$movementContext = context;
    }

    @Override
    public Pair<BlockPos, Float> cosmiccore$getBlockBreakingProgress() {
        return blockBreakingProgress;
    }

    @Override
    public void cosmiccore$setBlockBreakingProgress(Pair<BlockPos, Float> progress) {
        blockBreakingProgress = progress;
    }
}
