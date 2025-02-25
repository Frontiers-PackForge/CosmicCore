package com.ghostipedia.cosmiccore.common.blockentity;

import com.ghostipedia.cosmiccore.api.noctyx.NoctyxType;

import com.gregtechceu.gtceu.api.capability.recipe.IO;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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
    protected Table<NoctyxType, IO, List<BlockPos>> connections;
    @Getter
    @Setter
    protected NoctyxType ownType;

    public NoctyxBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        neighbors = new ArrayList<>();
        connections = HashBasedTable.create();
    }

    public Direction getUpwardFacing() {
        if (getBlockState().hasProperty(BlockStateProperties.FACING)) {
            return getBlockState().getValue(BlockStateProperties.FACING);
        }
        return Direction.UP;
    }

    // saving and loading

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("ownType")) {
            var ownType = tag.getInt("ownType");
            this.ownType = NoctyxType.values()[ownType];
        }
        if (tag.contains("neighbors")) {
            var neighbors = tag.getIntArray("neighbors");
            for (int i = 0; neighbors.length - i >= 3; i += 3) {
                this.neighbors.add(new BlockPos(neighbors[i], neighbors[i + 1], neighbors[i + 2]));
            }
        }
        if (tag.contains("connections")) {
            var connections = tag.getCompound("connections");
            var rows = connections.getInt("rows");
            for (int i = 0; i < rows; i++) {
                var rowTag = connections.getCompound("row" + i);
                var type = NoctyxType.values()[rowTag.getInt("type")];
                for (var io : IO.values()) {
                    var key = String.valueOf(io.ordinal());
                    if (!rowTag.contains(key)) {
                        continue;
                    }
                    var positions = rowTag.getIntArray(key);
                    var posList = new ObjectArrayList<BlockPos>(positions.length / 3);
                    this.connections.put(type, io, posList);
                    for (int j = 0; positions.length - j >= 3; j += 3) {
                        posList.add(new BlockPos(positions[j], positions[j + 1], positions[j + 2]));
                    }
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("ownType", ownType.ordinal());
        tag.putIntArray("neighbors", neighbors.stream().mapMultiToInt((pos, c) -> {
            c.accept(pos.getX());
            c.accept(pos.getY());
            c.accept(pos.getZ());
        }).toArray());
        var connections = new CompoundTag();
        tag.put("connections", connections);
        var typeIter = this.connections.rowKeySet().iterator();
        var ind = 0;
        while (typeIter.hasNext()) {
            var rowTag = new CompoundTag();
            connections.put("row" + ind, rowTag);

            var key = typeIter.next();
            rowTag.putInt("type", key.ordinal());
            var row = this.connections.row(key);
            for (var io : IO.values()) {
                var positions = row.get(io);
                if (positions == null) {
                    continue;
                }
                rowTag.putIntArray(String.valueOf(io.ordinal()), positions.stream().mapMultiToInt((pos, c) -> {
                    c.accept(pos.getX());
                    c.accept(pos.getY());
                    c.accept(pos.getZ());
                }).toArray());
            }
            ind++;
        }
        connections.putInt("rows", ind);
    }
}
