package com.ghostipedia.cosmiccore.client;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.compat.lso.LsoFoodCompat;

import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT)
public final class FoodTooltipHandler {

    private static final String NOURISHMENT_KEY = "effect.farmersdelight.nourishment";

    private static final String[] LSO_TEMPERATURE_KEYS = {
            "effect.legendarysurvivaloverhaul.hot_food",
            "effect.legendarysurvivaloverhaul.hot_drink",
            "effect.legendarysurvivaloverhaul.cold_food",
            "effect.legendarysurvivaloverhaul.cold_drink" };

    private FoodTooltipHandler() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemTooltip(ItemTooltipEvent event) {
        stripByResolvedName(event, NOURISHMENT_KEY);
        if (LsoFoodCompat.isLoaded()) {
            for (String key : LSO_TEMPERATURE_KEYS) {
                stripByResolvedName(event, key);
            }
        }
    }

    private static void stripByResolvedName(ItemTooltipEvent event, String langKey) {
        String name = Component.translatable(langKey).getString();
        if (name.equals(langKey)) return;
        event.getToolTip().removeIf(line -> line.getString().contains(name));
    }
}
