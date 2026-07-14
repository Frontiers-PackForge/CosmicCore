package com.ghostipedia.cosmiccore.common.block.crop;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

final class RainbowCaneBlock extends Block {

    static final IntegerProperty AGE = BlockStateProperties.AGE_15;
    private static final int MAX_HEIGHT = 5;
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);

    RainbowCaneBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isEmptyBlock(pos.above())) return;

        int height = 1;
        while (level.getBlockState(pos.below(height)).is(this)) {
            height++;
        }
        if (height >= MAX_HEIGHT) return;

        int age = state.getValue(AGE);
        if (age == 15) {
            level.setBlockAndUpdate(pos.above(), defaultBlockState());
            level.setBlock(pos, state.setValue(AGE, 0), 4);
        } else {
            level.setBlock(pos, state.setValue(AGE, age + 1), 4);
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (below.is(this)) return true;
        if (!below.is(CropBlockTags.RAINBOW_CANE_PLANTABLE_ON)) return false;

        BlockPos ground = pos.below();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.getFluidState(ground.relative(direction)).is(Fluids.WATER)) return true;
        }
        return false;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) {
            level.scheduleTick(pos, this, 1);
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }
}
