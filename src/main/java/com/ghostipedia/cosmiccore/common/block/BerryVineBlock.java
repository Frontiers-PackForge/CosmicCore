package com.ghostipedia.cosmiccore.common.block;

import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BerryVineBlock extends MurkFloraBlock {

    public BerryVineBlock(Properties props) {
        super(props);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (!level.isClientSide) {
            popResource(level, pos, new ItemStack(CosmicItems.ABYSS_BERRY.asItem()));
            level.playSound(null, pos, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.BLOCKS, 1f, 1f);
            level.setBlock(pos, CosmicBlocks.ABYSS_VINE_TIP.getDefaultState()
                    .setValue(WATERLOGGED, state.getValue(WATERLOGGED)), 3);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
