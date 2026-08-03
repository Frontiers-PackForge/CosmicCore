package com.ghostipedia.cosmiccore.common.dimension;

import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

public final class FirmamentPortalShape {

    private static final int FRAME_WIDTH = 4;
    private static final int FRAME_HEIGHT = 5;

    private FirmamentPortalShape() {}

    @Nullable
    public static Found find(BlockGetter level, BlockPos near) {
        for (Direction.Axis axis : new Direction.Axis[] { Direction.Axis.X, Direction.Axis.Z }) {
            Direction horizontal = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
            for (int horizontalOffset = 0; horizontalOffset < FRAME_WIDTH; horizontalOffset++) {
                for (int verticalOffset = 0; verticalOffset < FRAME_HEIGHT; verticalOffset++) {
                    BlockPos base = near.relative(horizontal, -horizontalOffset).below(verticalOffset);
                    if (isValid(level, base, horizontal)) {
                        return new Found(base, axis);
                    }
                }
            }
        }
        return null;
    }

    public static void fill(Level level, Found found) {
        Direction horizontal = found.axis() == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        BlockState portal = CosmicBlocks.FIRMAMENT_PORTAL.getDefaultState()
                .setValue(FirmamentPortalBlock.AXIS, found.axis());
        for (int width = 1; width <= 2; width++) {
            for (int height = 1; height <= 3; height++) {
                level.setBlock(found.base().relative(horizontal, width).above(height), portal, 18);
            }
        }
    }

    public static BlockPos build(Level level, BlockPos base, Direction.Axis axis) {
        Direction horizontal = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        Direction normal = axis == Direction.Axis.X ? Direction.SOUTH : Direction.EAST;
        BlockState frame = CosmicBlocks.FIRMAMENT_SAPROLITE.getDefaultState();
        for (int width = -1; width <= 4; width++) {
            for (int depth = -1; depth <= 1; depth++) {
                level.setBlock(base.relative(horizontal, width).relative(normal, depth).below(), frame, 18);
            }
        }
        for (int width = 0; width < FRAME_WIDTH; width++) {
            level.setBlock(base.relative(horizontal, width), frame, 18);
            level.setBlock(base.relative(horizontal, width).above(FRAME_HEIGHT - 1), frame, 18);
        }
        for (int height = 1; height < FRAME_HEIGHT - 1; height++) {
            level.setBlock(base.above(height), frame, 18);
            level.setBlock(base.relative(horizontal, FRAME_WIDTH - 1).above(height), frame, 18);
            for (int width = 1; width < FRAME_WIDTH - 1; width++) {
                level.setBlock(base.relative(horizontal, width).above(height), Blocks.AIR.defaultBlockState(), 18);
            }
        }
        fill(level, new Found(base, axis));
        return base.relative(horizontal, 1).above();
    }

    private static boolean isValid(BlockGetter level, BlockPos base, Direction horizontal) {
        for (int width = 0; width < FRAME_WIDTH; width++) {
            if (!isFrame(level.getBlockState(base.relative(horizontal, width))) ||
                    !isFrame(level.getBlockState(base.relative(horizontal, width).above(FRAME_HEIGHT - 1)))) {
                return false;
            }
        }
        for (int height = 1; height < FRAME_HEIGHT - 1; height++) {
            if (!isFrame(level.getBlockState(base.above(height))) ||
                    !isFrame(level.getBlockState(base.relative(horizontal, FRAME_WIDTH - 1).above(height)))) {
                return false;
            }
            for (int width = 1; width < FRAME_WIDTH - 1; width++) {
                BlockState state = level.getBlockState(base.relative(horizontal, width).above(height));
                if (!state.isAir() && !state.is(Blocks.WATER) && !state.is(CosmicBlocks.FIRMAMENT_PORTAL.get())) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isFrame(BlockState state) {
        return state.is(CosmicBlocks.FIRMAMENT_SAPROLITE.get());
    }

    public record Found(BlockPos base, Direction.Axis axis) {}
}
