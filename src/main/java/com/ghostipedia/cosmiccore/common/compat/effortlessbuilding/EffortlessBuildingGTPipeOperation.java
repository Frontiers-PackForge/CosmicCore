package com.ghostipedia.cosmiccore.common.compat.effortlessbuilding;

import net.minecraft.core.BlockPos;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public record EffortlessBuildingGTPipeOperation(
                                                Map<BlockPos, EffortlessBuildingGTPipeChange> changes,
                                                BlockPos firstPos,
                                                @Nullable EffortlessBuildingGTPipeAnchor anchor) {

    public EffortlessBuildingGTPipeOperation {
        changes = Map.copyOf(changes);
        firstPos = firstPos.immutable();
    }
}
