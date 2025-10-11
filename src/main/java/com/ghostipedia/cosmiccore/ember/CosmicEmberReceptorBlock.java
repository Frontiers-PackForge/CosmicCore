package com.ghostipedia.cosmiccore.ember;

import com.ghostipedia.cosmiccore.ember.blockentity.CosmicEmberReceptorBlockEntity;
import com.rekindled.embers.block.EmberReceiverBlock;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static com.ghostipedia.cosmiccore.common.data.CosmicBlockEntities.COSMIC_EMBER_RECEIVER_BE_STEAM;

public class CosmicEmberReceptorBlock extends EmberReceiverBlock {

    @Getter
    private int tier;

    public CosmicEmberReceptorBlock(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }


    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return pLevel.isClientSide ? null : createTickerHelper(pBlockEntityType, COSMIC_EMBER_RECEIVER_BE_STEAM.get(), CosmicEmberReceptorBlockEntity::serverTick);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return super.newBlockEntity(pPos, pState);
    }
}
