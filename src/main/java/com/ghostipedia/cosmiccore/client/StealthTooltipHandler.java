package com.ghostipedia.cosmiccore.client;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.murkbloom.StealthCoating;

import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT)
public final class StealthTooltipHandler {

    private StealthTooltipHandler() {}

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        int tier = StealthCoating.tier(event.getItemStack());
        if (tier <= 0) return;
        event.getToolTip().add(Component.translatable("cosmiccore.tooltip.stealth_coated",
                StealthCoating.numeral(tier)).withStyle(style -> style.withColor(0x9CC3D6)));
    }
}
