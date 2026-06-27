package com.ghostipedia.cosmiccore.common.reflection.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Soul Mutilator - an item that allows players to shape their soul.
 * Simply having this in your inventory enables soul shape selection in the Mirror of Erosion.
 * Consumed when a soul shape is chosen.
 */
public class SoulMutilatorItem extends Item {

    public SoulMutilatorItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        tooltip.add(Component.translatable("item.cosmiccore.soul_mutilator.tooltip")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.cosmiccore.soul_mutilator.tooltip.warning")
                .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
