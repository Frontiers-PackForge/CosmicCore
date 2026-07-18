package com.ghostipedia.cosmiccore.common.block.crop;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

final class SoulGourdCropBlock extends Block implements BonemealableBlock {

    static final IntegerProperty AGE = BlockStateProperties.AGE_7;
    private static final int MAX_AGE = 7;
    private static final VoxelShape[] SHAPES = {
            Block.box(7, 0, 7, 9, 2, 9), Block.box(6, 0, 6, 10, 4, 10),
            Block.box(5, 0, 5, 11, 6, 11), Block.box(4, 0, 4, 12, 8, 12),
            Block.box(3, 0, 3, 13, 10, 13), Block.box(2, 0, 2, 14, 12, 14),
            Block.box(1, 0, 1, 15, 14, 15), Block.box(1, 0, 1, 15, 16, 15)
    };

    private final Supplier<? extends net.minecraft.world.level.ItemLike> plantingItem;

    SoulGourdCropBlock(Properties properties,
                       Supplier<? extends net.minecraft.world.level.ItemLike> plantingItem) {
        super(properties);
        this.plantingItem = plantingItem;
        registerDefaultState(stateDefinition.any().setValue(AGE, 0));
    }

    boolean isMaxAge(BlockState state) {
        return state.getValue(AGE) == MAX_AGE;
    }

    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(CropBlockTags.SOUL_GOURD_PLANTABLE_ON);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return mayPlaceOn(level.getBlockState(pos.below()), level, pos.below());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return canSurvive(state, level, pos) ?
                super.updateShape(state, direction, neighborState, level, pos, neighborPos) :
                Blocks.AIR.defaultBlockState();
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getRawBrightness(pos, 0) < 9) return;
        int age = state.getValue(AGE);
        if (age < MAX_AGE) {
            if (random.nextInt(5) == 0) level.setBlock(pos, state.setValue(AGE, age + 1), 2);
        } else if (random.nextInt(4) == 0) {
            tryGrowFruit(level, pos, random);
        }
    }

    private void tryGrowFruit(ServerLevel level, BlockPos pos, RandomSource random) {
        List<Direction> candidates = new ArrayList<>();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos target = pos.relative(direction);
            if (level.isEmptyBlock(target) &&
                    level.getBlockState(target.below()).isFaceSturdy(level, target.below(), Direction.UP)) {
                candidates.add(direction);
            }
        }
        if (candidates.isEmpty()) return;
        Direction direction = candidates.get(random.nextInt(candidates.size()));
        BlockPos target = pos.relative(direction);
        level.setBlock(target, CosmicCrops.SOUL_GOURD_BLOOM.getDefaultState()
                .setValue(SoulGourdBloomBlock.FACING, direction.getOpposite())
                .setValue(SoulGourdBloomBlock.ATTACHED, true), 3);
        level.setBlock(pos, CosmicCrops.SOUL_GOURD_ATTACHED_STEM.getDefaultState()
                .setValue(SoulGourdAttachedStemBlock.FACING, direction), 3);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int age = state.getValue(AGE);
        if (age < MAX_AGE) {
            int grownAge = Math.min(MAX_AGE, age + 2 + random.nextInt(4));
            BlockState grownState = state.setValue(AGE, grownAge);
            level.setBlock(pos, grownState, 2);
            if (grownAge == MAX_AGE) tryGrowFruit(level, pos, random);
        } else {
            tryGrowFruit(level, pos, random);
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(plantingItem.get());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(AGE)];
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }
}

final class SoulGourdAttachedStemBlock extends Block {

    static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape NORTH_SHAPE = Block.box(6, 0, 0, 10, 13, 10);
    private static final VoxelShape EAST_SHAPE = Block.box(6, 0, 6, 16, 13, 10);
    private static final VoxelShape SOUTH_SHAPE = Block.box(6, 0, 6, 10, 13, 16);
    private static final VoxelShape WEST_SHAPE = Block.box(0, 0, 6, 10, 13, 10);

    SoulGourdAttachedStemBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (!level.getBlockState(pos.below()).is(CropBlockTags.SOUL_GOURD_PLANTABLE_ON)) return false;
        Direction direction = state.getValue(FACING);
        BlockState fruit = level.getBlockState(pos.relative(direction));
        return fruit.is(CosmicCrops.SOUL_GOURD_BLOOM.get()) &&
                fruit.getValue(SoulGourdBloomBlock.ATTACHED) &&
                fruit.getValue(SoulGourdBloomBlock.FACING) == direction.getOpposite();
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == state.getValue(FACING) && !canSurvive(state, level, pos)) {
            return CosmicCrops.SOUL_GOURD_CROP.getDefaultState().setValue(SoulGourdCropBlock.AGE, 7);
        }
        return canSurvive(state, level, pos) ?
                super.updateShape(state, direction, neighborState, level, pos, neighborPos) :
                Blocks.AIR.defaultBlockState();
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(CosmicCrops.SOUL_GOURD_SEEDS.get());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}

final class SoulGourdBloomBlock extends Block {

    static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 12, 14);

    SoulGourdBloomBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ATTACHED, false));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(ATTACHED) && direction == state.getValue(FACING) &&
                (!neighborState.is(CosmicCrops.SOUL_GOURD_ATTACHED_STEM.get()) ||
                        neighborState.getValue(SoulGourdAttachedStemBlock.FACING) != direction.getOpposite())) {
            state = state.setValue(ATTACHED, false);
        }
        return canSurvive(state, level, pos) ?
                super.updateShape(state, direction, neighborState, level, pos, neighborPos) :
                Blocks.AIR.defaultBlockState();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(CosmicCrops.SOUL_GOURD.get());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ATTACHED);
    }
}
