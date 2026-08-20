package com.ghostipedia.cosmiccore.common.compat.effortlessbuilding;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record EffortlessBuildingGTPipeAnchor(
                                             BlockPos pos, Direction direction, boolean connectedBefore,
                                             boolean connectedAfter, boolean blockedBefore, boolean blockedAfter) {}
