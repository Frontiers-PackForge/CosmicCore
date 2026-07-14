package com.ghostipedia.cosmiccore.common.block.crop;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

final class DriftweedRootBlock extends Block implements SimpleWaterloggedBlock, BonemealableBlock {

    static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final int MAX_STALKS = 3;
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 10, 13);

    DriftweedRootBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AGE, 0).setValue(WATERLOGGED, true));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
        if (!fluid.is(Fluids.WATER) || !fluid.isSource()) return null;
        return defaultBlockState().setValue(WATERLOGGED, true);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) != 0) return;
        advance(level, pos, state);
    }

    private void advance(ServerLevel level, BlockPos pos, BlockState state) {
        int age = state.getValue(AGE);
        if (age < 3) {
            level.setBlock(pos, state.setValue(AGE, age + 1), 2);
        } else {
            growToSurface(level, pos);
        }
    }

    private void growToSurface(ServerLevel level, BlockPos pos) {
        List<BlockPos> stalkPositions = new ArrayList<>();
        BlockPos cursor = pos.above();
        while (level.getBlockState(cursor).is(Blocks.WATER) && stalkPositions.size() < MAX_STALKS) {
            stalkPositions.add(cursor);
            cursor = cursor.above();
        }
        if (level.getBlockState(cursor).is(Blocks.WATER) || !level.isEmptyBlock(cursor)) return;
        for (BlockPos stalkPos : stalkPositions) {
            level.setBlock(stalkPos, CosmicCrops.DRIFTWEED_STALK.getDefaultState(), 3);
        }
        level.setBlock(cursor, CosmicCrops.DRIFTWEED_BLOOM.getDefaultState(), 3);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return state.getValue(WATERLOGGED) &&
                level.getBlockState(pos.below()).is(CropBlockTags.DRIFTWEED_PLANTABLE_ON);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        if (!canSurvive(state, level, pos)) return Blocks.WATER.defaultBlockState();
        if (direction == Direction.UP && neighborState.isAir()) level.scheduleTick(pos, this, 2);
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(AGE) == 3) growToSurface(level, pos);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.getValue(AGE) < 3 || level.isEmptyBlock(pos.above()) ||
                level.getFluidState(pos.above()).is(Fluids.WATER);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        advance(level, pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, WATERLOGGED);
    }
}

final class DriftweedStalkBlock extends Block implements SimpleWaterloggedBlock {

    static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape SHAPE = Block.box(6, 0, 6, 10, 16, 10);

    DriftweedStalkBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(WATERLOGGED, true));
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return state.getValue(WATERLOGGED) &&
                (below.is(CosmicCrops.DRIFTWEED_ROOT.get()) || below.is(this));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        if (!canSurvive(state, level, pos)) return Blocks.WATER.defaultBlockState();
        if (direction == Direction.UP && neighborState.isAir()) level.scheduleTick(pos, this, 2);
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.isEmptyBlock(pos.above())) {
            level.setBlock(pos.above(), CosmicCrops.DRIFTWEED_BLOOM.getDefaultState(), 3);
        }
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }
}

final class DriftweedBloomBlock extends HarvestableCropBlock {

    DriftweedBloomBlock(Properties properties, Supplier<? extends ItemLike> produce,
                        Supplier<? extends ItemLike> plantingItem) {
        super(properties, produce, plantingItem, true);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(CosmicCrops.DRIFTWEED_ROOT.get()) || below.is(CosmicCrops.DRIFTWEED_STALK.get());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return canSurvive(state, level, pos) ?
                super.updateShape(state, direction, neighborState, level, pos, neighborPos) :
                Blocks.AIR.defaultBlockState();
    }
}
