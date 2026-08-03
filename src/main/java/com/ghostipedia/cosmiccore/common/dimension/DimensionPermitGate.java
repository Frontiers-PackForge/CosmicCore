package com.ghostipedia.cosmiccore.common.dimension;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.network.chat.Component;
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

    private static final ResourceLocation FIRMAMENT_PERMIT_ADVANCEMENT = ResourceLocation
            .fromNamespaceAndPath(CosmicCore.MOD_ID, "firmament_permit");

    @SubscribeEvent
    public static void onDimensionTravel(EntityTravelToDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (event.getDimension().equals(Level.NETHER)) {
            gate(event, player, NETHER_PERMIT_ADVANCEMENT, "You need a Nether Permit to enter the Nether.");
        } else if (event.getDimension().equals(FirmamentDimension.KEY)) {
            gate(event, player, FIRMAMENT_PERMIT_ADVANCEMENT, "You need a Firmament Permit to enter the Firmament.");
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
