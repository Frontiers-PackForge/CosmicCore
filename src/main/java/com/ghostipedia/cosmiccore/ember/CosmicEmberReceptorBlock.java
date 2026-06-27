package com.ghostipedia.cosmiccore.ember;

import com.ghostipedia.cosmiccore.common.data.CosmicBlockEntities;
import com.ghostipedia.cosmiccore.ember.blockentity.CosmicEmberReceptorBlockEntity;

import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.rekindled.embers.block.EmberReceiverBlock;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.ghostipedia.cosmiccore.common.data.CosmicBlockEntities.COSMIC_EMBER_RECEIVER_BE;

public class CosmicEmberReceptorBlock extends EmberReceiverBlock {

    @Getter
    private int tier;

    public CosmicEmberReceptorBlock(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState,
                                                                  BlockEntityType<T> pBlockEntityType) {
        return pLevel.isClientSide ? null : createTickerHelper(pBlockEntityType,
                COSMIC_EMBER_RECEIVER_BE.get(tier).get(), CosmicEmberReceptorBlockEntity::serverTick);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new CosmicEmberReceptorBlockEntity(CosmicBlockEntities.COSMIC_EMBER_RECEIVER_BE.get(tier).get(), pPos,
                pState, tier);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        long transfer = (long) (250 * Math.pow(4, tier));
        long capacity = (long) (250 * Math.pow(4, tier + 2));
        tooltip.add(Component.translatable("cosmiccore.ember.capacity",
                Component.literal(FormattingUtil.formatNumberReadable(capacity)).withStyle(ChatFormatting.GOLD)));
        tooltip.add(Component.translatable("cosmiccore.ember.transfer",
                Component.literal(FormattingUtil.formatNumberReadable(transfer)).withStyle(ChatFormatting.GOLD)));
    }
}
