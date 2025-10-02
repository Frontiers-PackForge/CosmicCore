package com.ghostipedia.cosmiccore.ember;

import com.rekindled.embers.block.EmberEmitterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CosmicEmberEjector extends EmberEmitterBlock implements ICosmicEmberStats {

    private final double transfer;
    private final double pull;
    private final int capacity;



    public CosmicEmberEjector(Properties properties, double transfer, double pull, int capacity) {
        super(properties);
        this.transfer = transfer;
        this.pull = pull;
        this.capacity = capacity;
    }

    @Override public double transfer() { return transfer; }
    @Override public double pull() { return pull; }
    @Override public int capacity() { return capacity; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return super.newBlockEntity(pPos, pState);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return super.getTicker(pLevel, pState, pBlockEntityType);
    }


}
