package com.ghostipedia.cosmiccore.common.block.noctyx;

import com.ghostipedia.cosmiccore.common.blockentity.NoctyxBlockEntity;
import com.ghostipedia.cosmiccore.common.data.CosmicBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class NoctyxRelayBlock extends NoctyxBlock implements EntityBlock {

    protected static final Map<Direction, Vector3f> offsetMap = new EnumMap<>(Direction.class);
    static {
        // todo: fill out actual values according to model (this is the offset to the laser target)
        for (var direction : Direction.values()) {
            offsetMap.put(direction, defaultOffset);
        }
    }

    public NoctyxRelayBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull Vector3f getLaserOffset(Direction attachedSide) {
        return offsetMap.get(attachedSide);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return CosmicBlockEntities.NOCTYX_BLOCK_ENTITY.create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> blockEntityType) {
        return (l, p, s, blockEntity) -> ((NoctyxBlockEntity) blockEntity).serverTick();
    }
}
