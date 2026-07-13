package com.ghostipedia.cosmiccore.common.data.temperature.attribute;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import sfiomn.legendarysurvivaloverhaul.registry.AttributeRegistry;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class LegacyFireResistanceCleanup {

    private static final ResourceLocation LEGACY_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(
            "legendarysurvivaloverhaul", "heat_resistance_9b3cd493");

    private LegacyFireResistanceCleanup() {}

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var heatResistance = player.getAttribute(AttributeRegistry.HEAT_RESISTANCE);
        if (heatResistance != null) {
            heatResistance.removeModifier(LEGACY_MODIFIER_ID);
        }
    }
}
