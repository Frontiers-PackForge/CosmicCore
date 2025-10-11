package com.ghostipedia.cosmiccore.ember;

import com.ghostipedia.cosmiccore.common.data.CosmicBlockEntities;
import com.ghostipedia.cosmiccore.ember.blockentity.CosmicEmberEmitterBlockEntity;
import com.rekindled.embers.RegistryManager;
import com.rekindled.embers.block.EmberEmitterBlock;
import com.rekindled.embers.blockentity.EmberEmitterBlockEntity;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CosmicEmberEmitterBlock extends EmberEmitterBlock {

    @Getter
    private int tier;

    public CosmicEmberEmitterBlock(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return pLevel.isClientSide ? null : createTickerHelper(pBlockEntityType, CosmicBlockEntities.COSMIC_EMBER_EMITTER_BE.get(), CosmicEmberEmitterBlockEntity::serverTick);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new CosmicEmberEmitterBlockEntity(CosmicBlockEntities.COSMIC_EMBER_EMITTER_BE.get(), pPos, pState, tier);
    }
}
