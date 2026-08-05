package com.ghostipedia.cosmiccore.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

public class LargeArcaniteClusterBlock extends MurkFloraBlock {

    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final BooleanProperty LEGACY = BooleanProperty.create("legacy");
    private static final VoxelShape HALF_SHAPE = Block.box(1, 0, 1, 15, 16, 15);
    private static final VoxelShape LEGACY_SHAPE = Block.box(1, 0, 1, 15, 32, 15);

    public LargeArcaniteClusterBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any()
                .setValue(WATERLOGGED, true)
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(LEGACY, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HALF, LEGACY);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() >= level.getMaxBuildHeight() - 1 || !level.getBlockState(pos.above()).canBeReplaced(context)) {
            return null;
        }
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : state.setValue(HALF, DoubleBlockHalf.LOWER).setValue(LEGACY, false);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        BlockPos upperPos = pos.above();
        BlockState upper = defaultBlockState()
                .setValue(HALF, DoubleBlockHalf.UPPER)
                .setValue(LEGACY, false)
                .setValue(WATERLOGGED, level.isWaterAt(upperPos));
        level.setBlock(upperPos, upper, 3);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        BlockState updated = super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        if (state.getValue(LEGACY) || direction.getAxis() != Direction.Axis.Y) {
            return updated;
        }
        DoubleBlockHalf half = state.getValue(HALF);
        boolean towardPartner = half == DoubleBlockHalf.LOWER ? direction == Direction.UP : direction == Direction.DOWN;
        if (!towardPartner) {
            return updated;
        }
        if (neighborState.is(this) && !neighborState.getValue(LEGACY) && neighborState.getValue(HALF) != half) {
            return updated;
        }
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false).createLegacyBlock() :
                net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(LEGACY) ? LEGACY_SHAPE : HALF_SHAPE;
    }

    public static void placeAt(ChunkAccess chunk, BlockPos pos, BlockState state) {
        BlockPos upperPos = pos.above();
        chunk.setBlockState(pos, state
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(LEGACY, false)
                .setValue(WATERLOGGED, chunk.getFluidState(pos).is(Fluids.WATER)), false);
        chunk.setBlockState(upperPos, state
                .setValue(HALF, DoubleBlockHalf.UPPER)
                .setValue(LEGACY, false)
                .setValue(WATERLOGGED, chunk.getFluidState(upperPos).is(Fluids.WATER)), false);
    }
}
