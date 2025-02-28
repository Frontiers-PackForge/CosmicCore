package com.ghostipedia.cosmiccore.common.blockentity;

import com.ghostipedia.cosmiccore.api.noctyx.NoctyxType;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NoctyxBlockEntity extends BlockEntity {

    @Getter
    protected List<BlockPos> neighbors;
    @Getter
    @Setter
    protected NoctyxType ownType;

    public NoctyxBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        neighbors = new ArrayList<>();
    }

    public Direction getUpwardFacing() {
        if (getBlockState().hasProperty(BlockStateProperties.FACING)) {
            return getBlockState().getValue(BlockStateProperties.FACING);
        }
        return Direction.UP;
    }

    public void serverTick() {
        // todo: implement network things
    }

    // saving and loading

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("ownType")) {
            var ownTypeTag = tag.getCompound("ownType");
            this.ownType = new NoctyxType();
            this.ownType.deserializeNBT(ownTypeTag);
        }
        if (tag.contains("neighbors")) {
            var neighbors = tag.getIntArray("neighbors");
            for (int i = 0; neighbors.length - i >= 3; i += 3) {
                this.neighbors.add(new BlockPos(neighbors[i], neighbors[i + 1], neighbors[i + 2]));
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("ownType", ownType.serializeNBT());
        tag.putIntArray("neighbors", neighbors.stream().mapMultiToInt((pos, c) -> {
            c.accept(pos.getX());
            c.accept(pos.getY());
            c.accept(pos.getZ());
        }).toArray());
    }
}
