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
            player.sendSystemMessage(displaySoulNetworkInfo(soulNetwork));
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    public static Component displaySoulNetworkInfo(SoulNetwork network) {
        var message = Component.empty();
        List<SoulStack> contents = network.getContents();
        if (contents.isEmpty()) {
            message.append(Component.translatable("gui.cosmiccore.soul.empty_network").withStyle(ChatFormatting.GRAY));
        } else {
            message.append(Component.translatable("gui.cosmiccore.soul.network_contents").withStyle(ChatFormatting.GOLD)).append("\n");
            message.append(Component.translatable("gui.cosmiccore.soul.capacity", network.getSize()).withStyle(ChatFormatting.GRAY));
            for (SoulStack stack : contents) message.append("\n").append(stack.type().toComponent(stack.amount()));
        }
        return message;
    }

}
