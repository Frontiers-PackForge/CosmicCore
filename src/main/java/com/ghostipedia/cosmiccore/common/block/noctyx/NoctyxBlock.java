package com.ghostipedia.cosmiccore.common.block.noctyx;

import com.ghostipedia.cosmiccore.common.data.CosmicBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import javax.annotation.ParametersAreNonnullByDefault;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

@SuppressWarnings("deprecation")
@ParametersAreNonnullByDefault
public abstract class NoctyxBlock extends Block implements EntityBlock {

    protected static final Vector3f defaultOffset = new Vector3f(.5f, .5f, .5f);

    public NoctyxBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        var direction = state.getValue(FACING);
        var p = pos.relative(direction.getOpposite());
        var s = level.getBlockState(pos);
        return s.isFaceSturdy(level, p, direction);
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var state = this.defaultBlockState();
        var level = context.getLevel();
        var blockpos = context.getClickedPos();
        var directions = context.getNearestLookingDirections();

        for (var direction : directions) {
            var opposite = direction.getOpposite();
            state = state.setValue(FACING, opposite);
            if (state.canSurvive(level, blockpos)) {
                return state;
            }
        }
        return null;
    }

    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return CosmicBlockEntities.NOCTYX_BLOCK_ENTITY.create(pos, state);
    }

    /**
     * @param attachedSide the direction the relay is attachedSide
     * @return laser target offset from center of the block
     */
    public abstract @NotNull Vector3f getLaserOffset(Direction attachedSide);
}
