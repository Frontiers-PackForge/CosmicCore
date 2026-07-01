package com.ghostipedia.cosmiccore.client;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT)
public final class FoodTooltipHandler {

    private static final String NOURISHMENT_KEY = "effect.farmersdelight.nourishment";

    private FoodTooltipHandler() {}

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        String name = Component.translatable(NOURISHMENT_KEY).getString();
        if (name.equals(NOURISHMENT_KEY)) return;
        event.getToolTip().removeIf(line -> line.getString().contains(name));
    }
}
