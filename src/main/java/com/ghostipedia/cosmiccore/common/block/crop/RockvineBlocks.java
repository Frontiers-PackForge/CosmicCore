package com.ghostipedia.cosmiccore.common.block.crop;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CaveVinesBlock;
import net.minecraft.world.level.block.CaveVinesPlantBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

final class RockvineHarvest {

    static final int MAX_HEIGHT = 5;

    private RockvineHarvest() {}

    static InteractionResult use(BlockState state, Level level, BlockPos pos) {
        if (!state.getValue(BlockStateProperties.BERRIES)) return InteractionResult.PASS;
        if (!level.isClientSide) {
            Block.popResource(level, pos, new ItemStack(CosmicCrops.ROCKVINE_BERRY.get()));
            level.setBlock(pos, state.setValue(BlockStateProperties.BERRIES, false), 2);
            level.playSound(null, pos, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.BLOCKS, 1f, 1f);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    static int columnHeight(LevelReader level, BlockPos pos) {
        int height = 1;
        BlockPos cursor = pos.above();
        while (isRockvine(level.getBlockState(cursor))) {
            height++;
            cursor = cursor.above();
        }
        cursor = pos.below();
        while (isRockvine(level.getBlockState(cursor))) {
            height++;
            cursor = cursor.below();
        }
        return height;
    }

    private static boolean isRockvine(BlockState state) {
        return state.is(CosmicCrops.CULTIVATED_ROCKVINE_BODY.get()) ||
                state.is(CosmicCrops.CULTIVATED_ROCKVINE_BLOOM.get());
    }
}

final class RockvineBodyBlock extends CaveVinesPlantBlock {

    RockvineBodyBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected GrowingPlantHeadBlock getHeadBlock() {
        return CosmicCrops.CULTIVATED_ROCKVINE_BLOOM.get();
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(CosmicCrops.ROCKVINE_BERRY.get());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        return RockvineHarvest.use(state, level, pos);
    }
}

final class RockvineBloomBlock extends CaveVinesBlock {

    RockvineBloomBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected Block getBodyBlock() {
        return CosmicCrops.CULTIVATED_ROCKVINE_BODY.get();
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (RockvineHarvest.columnHeight(level, pos) < RockvineHarvest.MAX_HEIGHT) {
            super.randomTick(state, level, pos, random);
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(CosmicCrops.ROCKVINE_BERRY.get());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        return RockvineHarvest.use(state, level, pos);
    }
}
