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
            gate(event, player, NETHER_PERMIT_ADVANCEMENT, "cosmiccore.dimension.nether_permit_required");
        } else if (event.getDimension().equals(FirmamentDimension.KEY)) {
            if (!canEnterFirmament(player, true)) event.setCanceled(true);
        }
    }

    private static void gate(EntityTravelToDimensionEvent event, ServerPlayer player,
                             ResourceLocation advancementId, String messageKey) {
        var advancement = player.server.getAdvancements().get(advancementId);
        if (advancement == null) return;
        if (!player.getAdvancements().getOrStartProgress(advancement).isDone()) {
            event.setCanceled(true);
            player.displayClientMessage(Component.translatable(messageKey), true);
        }
    }

    public static boolean canEnterFirmament(ServerPlayer player, boolean notify) {
        var advancement = player.server.getAdvancements().get(FIRMAMENT_PERMIT_ADVANCEMENT);
        boolean permitted = advancement == null || player.getAdvancements().getOrStartProgress(advancement).isDone();
        if (!permitted && notify) {
            player.displayClientMessage(
                    Component.translatable("cosmiccore.dimension.firmament_permit_required"), true);
        }
        return permitted;
    }
}
