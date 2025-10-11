package com.ghostipedia.cosmiccore.ember;

import com.ghostipedia.cosmiccore.common.data.CosmicBlockEntities;
import com.ghostipedia.cosmiccore.ember.blockentity.CosmicEmberEmitterBlockEntity;

import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.rekindled.embers.block.EmberEmitterBlock;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CosmicEmberEmitterBlock extends EmberEmitterBlock {

    @Getter
    private int tier;

    public CosmicEmberEmitterBlock(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState,
                                                                  BlockEntityType<T> pBlockEntityType) {
        return pLevel.isClientSide ? null : createTickerHelper(pBlockEntityType,
                CosmicBlockEntities.COSMIC_EMBER_EMITTER_BE.get(tier).get(), CosmicEmberEmitterBlockEntity::serverTick);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new CosmicEmberEmitterBlockEntity(CosmicBlockEntities.COSMIC_EMBER_EMITTER_BE.get(tier).get(), pPos,
                pState, tier);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
                                TooltipFlag flag) {
        long capacity = (long) (1000 * Math.pow(4, tier + 2));
        long transfer = (long) (1000 * Math.pow(4, tier));
        tooltip.add(Component.translatable("cosmiccore.ember.capacity",
                Component.literal(FormattingUtil.formatNumberReadable(capacity)).withStyle(ChatFormatting.GOLD)));
        tooltip.add(Component.translatable("cosmiccore.ember.transfer",
                Component.literal(FormattingUtil.formatNumberReadable(transfer)).withStyle(ChatFormatting.GOLD)));
    }
}
