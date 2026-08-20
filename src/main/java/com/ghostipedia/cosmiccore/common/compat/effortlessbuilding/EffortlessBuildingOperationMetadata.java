package com.ghostipedia.cosmiccore.common.compat.effortlessbuilding;

import net.minecraft.core.BlockPos;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public record EffortlessBuildingOperationMetadata(
                                                  Map<BlockPos, EffortlessBuildingBlockChange> changes,
                                                  @Nullable EffortlessBuildingGTPipeOperation pipeOperation) {

    public EffortlessBuildingOperationMetadata {
        changes = Map.copyOf(changes);
    }
}
