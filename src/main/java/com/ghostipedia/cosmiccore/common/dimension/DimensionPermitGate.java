package com.ghostipedia.cosmiccore.common.dimension;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;

@EventBusSubscriber(modid = CosmicCore.MOD_ID)
public class DimensionPermitGate {

    private static final ResourceLocation NETHER_PERMIT_ADVANCEMENT = ResourceLocation
            .fromNamespaceAndPath(CosmicCore.MOD_ID, "nether_permit");

    private static final ResourceLocation AETHER_PERMIT_ADVANCEMENT = ResourceLocation
            .fromNamespaceAndPath(CosmicCore.MOD_ID, "aether_permit");

    private static final ResourceKey<Level> AETHER_DIMENSION = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath("aether", "the_aether"));

    @SubscribeEvent
    public static void onDimensionTravel(EntityTravelToDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (event.getDimension().equals(Level.NETHER)) {
            gate(event, player, NETHER_PERMIT_ADVANCEMENT, "You need a Nether Permit to enter the Nether.");
        } else if (event.getDimension().equals(AETHER_DIMENSION)) {
            gate(event, player, AETHER_PERMIT_ADVANCEMENT, "You need an Aether Permit to enter the Aether.");
        }
    }

    private static void gate(EntityTravelToDimensionEvent event, ServerPlayer player,
                             ResourceLocation advancementId, String message) {
        var advancement = player.server.getAdvancements().get(advancementId);
        if (advancement == null) return;
        if (!player.getAdvancements().getOrStartProgress(advancement).isDone()) {
            event.setCanceled(true);
            player.displayClientMessage(Component.literal(message), true);
        }
    }
}
