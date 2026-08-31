package com.ghostipedia.cosmiccore.common.dimension;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedLedger;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedRegistry;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedTeams;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;

@EventBusSubscriber(modid = CosmicCore.MOD_ID)
public class DimensionPermitGate {

    @SubscribeEvent
    public static void onDimensionTravel(EntityTravelToDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        DeedLedger deedLedger = DeedLedger.get(player.getServer());
        String teamKey = DeedTeams.teamKey(player);
        if (event.getDimension().equals(Level.NETHER) &&
                !deedLedger.isWoven(teamKey, DeedRegistry.NETHER_PERMIT.id())) {
            event.setCanceled(true);
            // TODO: better message for the player
            player.displayClientMessage(Component.translatable("cosmiccore.dimension.nether_permit_required"), true);
        }
    }

    // TODO(firmament): implement firmament gating when the deed is obtainable
}
