package com.ghostipedia.cosmiccore.client.transmission;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.integration.map.WaypointManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import com.glodblock.github.extendedae.client.render.EAEHighlightHandler;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class PowerTowerMELocator {

    private static final Map<ClientPacketListener, UUID> WAYPOINT_SESSIONS = new WeakHashMap<>();

    private PowerTowerMELocator() {}

    public static boolean locate(BlockPos pos, ResourceKey<Level> dimension, String circuitName, boolean input) {
        Component waypointName = Component.translatable("cosmiccore.tower_me." +
                (input ? "input_waypoint" : "output_waypoint"),
                circuitName.isBlank() ? Component.translatable("cosmiccore.tower_me.unnamed") :
                        Component.literal(circuitName));
        return locate(pos, dimension, waypointName, "tower_me",
                Component.translatable("cosmiccore.tower_me.waypoint_added", pos.toShortString()),
                Component.translatable("cosmiccore.tower_me.locate_unavailable", pos.toShortString()));
    }

    public static boolean locateTower(BlockPos pos, ResourceKey<Level> dimension) {
        return locate(pos, dimension, Component.translatable("cosmiccore.power_tower.ui.waypoint"), "power_tower",
                Component.translatable("cosmiccore.power_tower.ui.waypoint_added", pos.toShortString()),
                Component.translatable("cosmiccore.power_tower.ui.locate_unavailable", pos.toShortString()));
    }

    private static boolean locate(BlockPos pos, ResourceKey<Level> dimension, Component waypointName, String group,
                                  Component waypointFeedback, Component unavailableFeedback) {
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        var player = minecraft.player;
        if (level == null || player == null || minecraft.getConnection() == null ||
                !level.dimension().equals(dimension))
            return false;

        int viewDistance = minecraft.options.getEffectiveRenderDistance();
        var origin = player.chunkPosition();
        int chunkDistance = Math.max(Math.abs((pos.getX() >> 4) - origin.x),
                Math.abs((pos.getZ() >> 4) - origin.z));
        if (chunkDistance < viewDistance && level.hasChunkAt(pos) && GTCEu.isModLoaded("extendedae")) {
            ExtendedAE.highlight(pos, dimension);
            return true;
        }
        if (WaypointManager.isActive()) {
            var session = WAYPOINT_SESSIONS.computeIfAbsent(minecraft.getConnection(), ignored -> UUID.randomUUID());
            String key = "cosmiccore:" + group + "/" + session + "/" + dimension.location() + "/" + pos.asLong();
            WaypointManager.setWaypoint(key, waypointName.getString(), 0x59C9E8, dimension, pos);
            player.displayClientMessage(waypointFeedback, true);
            return true;
        }
        player.displayClientMessage(unavailableFeedback, false);
        return false;
    }

    private static final class ExtendedAE {

        private static void highlight(BlockPos pos, ResourceKey<Level> dimension) {
            EAEHighlightHandler.highlight(pos, dimension, System.currentTimeMillis() + 10_000);
        }
    }
}
