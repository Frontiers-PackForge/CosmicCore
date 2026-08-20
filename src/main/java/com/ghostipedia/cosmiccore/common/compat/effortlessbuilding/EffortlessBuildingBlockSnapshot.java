package com.ghostipedia.cosmiccore.common.compat.effortlessbuilding;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record EffortlessBuildingBlockSnapshot(BlockState state, @Nullable CompoundTag blockEntityTag) {

    public EffortlessBuildingBlockSnapshot {
        blockEntityTag = blockEntityTag == null ? null : blockEntityTag.copy();
    }

    public static EffortlessBuildingBlockSnapshot capture(ServerLevel level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        CompoundTag tag = blockEntity == null ? null : blockEntity.saveWithFullMetadata(level.registryAccess());
        return new EffortlessBuildingBlockSnapshot(level.getBlockState(pos), tag);
    }

    public boolean restore(ServerLevel level, BlockPos pos) {
        if (!level.setBlock(pos, state, 3) && !level.getBlockState(pos).equals(state)) return false;
        if (blockEntityTag == null) return true;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return false;
        blockEntity.loadWithComponents(blockEntityTag.copy(), level.registryAccess());
        blockEntity.setChanged();
        return true;
    }

    public boolean matches(ServerLevel level, BlockPos pos) {
        EffortlessBuildingBlockSnapshot current = capture(level, pos);
        return state.equals(current.state()) && Objects.equals(blockEntityTag, current.blockEntityTag());
    }
}
