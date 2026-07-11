package com.ghostipedia.cosmiccore.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StealthCoatingItem extends Item {

    @Getter
    private final int tier;

    public StealthCoatingItem(Properties props, int tier) {
        super(props);
        this.tier = tier;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("cosmiccore.tooltip.stealth_coating.use_1")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("cosmiccore.tooltip.stealth_coating.use_2")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
