package com.ghostipedia.cosmiccore.common.qol;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class NaturalItemPickupRange {

    private static final double EXTRA_RANGE = 0.5;

    private NaturalItemPickupRange() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator() || player.getHealth() <= 0.0F) {
            return;
        }
        AABB vanillaRange = player.isPassenger() && !player.getVehicle().isRemoved() ?
                player.getBoundingBox().minmax(player.getVehicle().getBoundingBox()).inflate(1.0, 0.0, 1.0) :
                player.getBoundingBox().inflate(1.0, 0.5, 1.0);
        AABB extendedRange = vanillaRange.inflate(EXTRA_RANGE);
        player.level()
                .getEntitiesOfClass(ItemEntity.class, extendedRange,
                        item -> !item.isRemoved() && !vanillaRange.intersects(item.getBoundingBox()))
                .forEach(item -> item.playerTouch(player));
    }
}
