package com.ghostipedia.cosmiccore.common.item;

import com.ghostipedia.cosmiccore.api.data.souls.SoulNetwork;
import com.ghostipedia.cosmiccore.api.data.souls.SoulNetworkSavedData;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class SoulNetworkReaderItem extends Item {

    public SoulNetworkReaderItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            SoulNetwork soulNetwork = SoulNetworkSavedData.getSoulNetwork((ServerLevel) level, player.getUUID());
            List<SoulStack> contents = soulNetwork.getContents();

            if (contents.isEmpty()) {
                player.sendSystemMessage(Component.literal("Network is empty.").withStyle(ChatFormatting.GRAY));
            } else {
                player.sendSystemMessage(Component.literal("--- Soul Network Contents ---").withStyle(ChatFormatting.GOLD));
                for (SoulStack stack : contents) player.sendSystemMessage(stack.type().toComponent(stack.amount()));
            }
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}
