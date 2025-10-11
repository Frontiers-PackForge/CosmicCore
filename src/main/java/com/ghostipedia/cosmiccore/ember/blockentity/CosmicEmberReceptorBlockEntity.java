package com.ghostipedia.cosmiccore.ember.blockentity;

import com.ghostipedia.cosmiccore.ember.ICosmicEmberStats;
import com.rekindled.embers.api.capabilities.EmbersCapabilities;
import com.rekindled.embers.api.power.IEmberCapability;
import com.rekindled.embers.blockentity.EmberReceiverBlockEntity;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class CosmicEmberReceptorBlockEntity extends EmberReceiverBlockEntity implements ICosmicEmberStats {

    @Getter
    private int tier;

    @Override
    public double transfer() {
        return 100 * (tier + 1);
    }
    //Unused
    @Override
    public double pull() {
        return 0;
    }

    public CosmicEmberReceptorBlockEntity(BlockEntityType<?> type, BlockPos pPos, BlockState pBlockState, int tier) {
        super(type,pPos, pBlockState);
        capability.setEmberCapacity(2500 * tier);
        this.tier = tier;
    }

    public static CosmicEmberReceptorBlockEntity create(BlockEntityType<?> type, BlockPos pPos, BlockState pBlockState, int tier) {
        return new CosmicEmberReceptorBlockEntity(type, pPos, pBlockState, tier);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CosmicEmberReceptorBlockEntity blockEntity) {
        blockEntity.ticksExisted ++;
        Direction facing = state.getValue(BlockStateProperties.FACING);
        BlockEntity attachedTile = level.getBlockEntity(pos.relative(facing, -1));
        if (blockEntity.ticksExisted % 2 == 0 && attachedTile != null){
            IEmberCapability cap = attachedTile.getCapability(EmbersCapabilities.EMBER_CAPABILITY, facing).orElse(null);
            if (cap != null) {
                if (cap.getEmber() < cap.getEmberCapacity() && blockEntity.capability.getEmber() > 0){
                    double added = cap.addAmount(Math.min(blockEntity.transfer(), blockEntity.capability.getEmber()), true);
                    blockEntity.capability.removeAmount(added, true);
                }
            }
        }
    }



}
