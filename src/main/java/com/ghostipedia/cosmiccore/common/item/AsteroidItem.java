package com.ghostipedia.cosmiccore.common.item;

import com.gregtechceu.gtceu.api.item.ComponentItem;

import com.ghostipedia.cosmiccore.utils.ItemData;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class AsteroidItem extends ComponentItem {

    public AsteroidItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        CompoundTag tag = ItemData.readTag(stack);

        if (tag.isEmpty()) {
            tooltipComponents.add(Component.translatable("tooltip.cosmiccore.asteroid.tiny")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            return;
        }

        int size = tag.getInt("Size");

        if (size > 0) {
            tooltipComponents.add(Component.translatable("tooltip.cosmiccore.asteroid.tier", size)
                    .withStyle(ChatFormatting.AQUA));
        }

        super.appendHoverText(stack, context, tooltipComponents, isAdvanced);
    }
}
