package com.ghostipedia.cosmiccore.client;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.compat.ars.ArsSealCompat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT)
public final class ArsSealClient {

    private ArsSealClient() {}

    private static boolean attuned;

    public static void setAttuned(boolean newAttuned) {
        attuned = newAttuned;
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (attuned) return;
        if (!ArsSealCompat.SEALED_BOOKS.contains(BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem()))) {
            return;
        }
        event.getToolTip().add(Component.translatable("cosmiccore.tooltip.tome_sealed_1")
                .withStyle(style -> style.withColor(0x8F7FB8).withItalic(true)));
        event.getToolTip().add(Component.translatable("cosmiccore.tooltip.tome_sealed_2")
                .withStyle(style -> style.withColor(0x8F7FB8).withItalic(true)));
    }
}
