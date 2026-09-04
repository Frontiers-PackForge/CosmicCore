package com.ghostipedia.cosmiccore.common.deployment;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

public record LeylineBlockPlacement(BlockPos relativeOffset, BlockState state) {

    public LeylineBlockPlacement {
        Objects.requireNonNull(relativeOffset, "relativeOffset");
        Objects.requireNonNull(state, "state");
        relativeOffset = relativeOffset.immutable();
    }
}
