package com.ghostipedia.cosmiccore.common.block;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class ComputationBayCasingBlock extends Block {

    public ComputationBayCasingBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("cosmiccore.block.me_computation_bay_casing.tooltip.0")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("cosmiccore.block.me_computation_bay_casing.tooltip.1")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
