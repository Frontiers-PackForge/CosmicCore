package com.ghostipedia.cosmiccore.mixin.sable;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.integration.sable.SableAssemblyRotation;
import com.ghostipedia.cosmiccore.integration.sable.SableAssemblyRotationHolder;

import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import dev.ryanhcode.sable.api.block.BlockSubLevelAssemblyListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = PipeBlock.class, remap = false)
public abstract class PipeBlockSubLevelMixin implements BlockSubLevelAssemblyListener {

    @Shadow
    public abstract void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random);

    @Shadow
    public abstract LevelPipeNet<?, ?> getWorldPipeNet(ServerLevel level);

    @Override
    public void afterMove(ServerLevel originLevel, ServerLevel resultingLevel, BlockState state, BlockPos oldPos,
                          BlockPos newPos) {
        if (getWorldPipeNet(resultingLevel).getNetFromPos(newPos) == null) {
            tick(state, resultingLevel, newPos, resultingLevel.getRandom());
        }
        if (resultingLevel.getBlockEntity(newPos) instanceof PipeBlockEntity<?, ?> pipe) {
            try {
                SableAssemblyRotation.rotatePipe(pipe, SableAssemblyRotationHolder.current(),
                        resultingLevel.registryAccess());
            } catch (Throwable t) {
                CosmicCore.LOGGER.error("Failed to rotate pipe connections/covers during Sable assembly at {}",
                        newPos, t);
            }
            pipe.getSyncDataHolder().resyncAllFields();
            pipe.scheduleRenderUpdate();
        }
    }
}
