package com.ghostipedia.cosmiccore.mixin.sable;

import com.ghostipedia.cosmiccore.integration.sable.SableAssemblyRotationHolder;

import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import dev.ryanhcode.sable.api.block.BlockSubLevelAssemblyListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

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
            cosmiccore$rotateConnections(pipe);
            pipe.getSyncDataHolder().resyncAllFields();
            pipe.scheduleRenderUpdate();
        }
    }

    @Unique
    private static void cosmiccore$rotateConnections(PipeBlockEntity<?, ?> pipe) {
        Rotation rotation = SableAssemblyRotationHolder.current();
        if (rotation == Rotation.NONE) {
            return;
        }
        int connections = cosmiccore$rotateMask(pipe.getConnections(), rotation);
        if (connections != pipe.getConnections()) {
            pipe.setConnections(connections);
        }
        int blocked = cosmiccore$rotateMask(pipe.getBlockedConnections(), rotation);
        if (blocked != pipe.getBlockedConnections()) {
            pipe.setBlockedConnections(blocked);
        }
    }

    @Unique
    private static int cosmiccore$rotateMask(int mask, Rotation rotation) {
        int result = 0;
        for (Direction dir : Direction.values()) {
            if (PipeBlockEntity.isConnected(mask, dir)) {
                result |= 1 << rotation.rotate(dir).ordinal();
            }
        }
        return result;
    }
}
