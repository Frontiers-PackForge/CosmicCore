package com.ghostipedia.cosmiccore.common.flight;

import com.ghostipedia.cosmiccore.common.data.CosmicItems;

import net.minecraft.core.GlobalPos;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class FlightDiffuserBehavior {

    private static final String IMMUNITY_KEY = "cosmiccore:flight_diffuser_landing_protection";
    private static final int BUFFER = 10;
    private static final Map<MinecraftServer, Map<UUID, FlightState>> SERVER_STATES = new WeakHashMap<>();

    private FlightDiffuserBehavior() {}

    public static void refresh(ServerPlayer player, GlobalPos source) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        FlightState state = states(server).computeIfAbsent(player.getUUID(), ignored -> new FlightState());
        state.sources.put(source, server.getTickCount() + BUFFER);
        reconcile(player, state, server.getTickCount());
    }

    public static void tick(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        Map<UUID, FlightState> states = states(server);
        FlightState state = states.get(player.getUUID());
        if (state != null) {
            reconcile(player, state, server.getTickCount());
            if (!state.covered && !state.ownsFlight && state.sources.isEmpty()) {
                states.remove(player.getUUID());
            }
        }
        if (player.onGround()) {
            clearLandingProtection(player);
        }
    }

    public static void removeSource(MinecraftServer server, GlobalPos source) {
        Map<UUID, FlightState> states = states(server);
        for (var entry : states.entrySet()) {
            FlightState state = entry.getValue();
            if (state.sources.remove(source) == null) continue;
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player != null) {
                reconcile(player, state, server.getTickCount());
            }
        }
    }

    public static boolean claimActiveFlight(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return false;
        FlightState state = states(server).get(player.getUUID());
        if (state == null) return false;
        expire(state, server.getTickCount());
        if (state.sources.isEmpty()) return false;
        state.covered = true;
        state.ownsFlight = true;
        grant(player);
        return true;
    }

    public static void clear(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        FlightState state = states(server).remove(player.getUUID());
        if (state != null && state.ownsFlight && !hasIndependentFlight(player)) {
            revoke(player);
        }
    }

    public static boolean consumeLandingProtection(ServerPlayer player) {
        if (!player.getPersistentData().getBoolean(IMMUNITY_KEY)) return false;
        clearLandingProtection(player);
        return true;
    }

    private static Map<UUID, FlightState> states(MinecraftServer server) {
        return SERVER_STATES.computeIfAbsent(server, ignored -> new HashMap<>());
    }

    private static void reconcile(ServerPlayer player, FlightState state, int tick) {
        expire(state, tick);
        boolean covered = !state.sources.isEmpty();
        if (covered) {
            clearLandingProtection(player);
            if (!state.covered) {
                state.covered = true;
                state.ownsFlight = !player.getAbilities().mayfly && !hasIndependentFlight(player);
            }
            if (hasIndependentFlight(player)) {
                state.ownsFlight = false;
            } else if (state.ownsFlight || !player.getAbilities().mayfly) {
                state.ownsFlight = true;
                grant(player);
            }
            return;
        }
        state.covered = false;
        if (state.ownsFlight && !hasIndependentFlight(player)) {
            revoke(player);
        }
        state.ownsFlight = false;
    }

    private static void expire(FlightState state, int tick) {
        state.sources.entrySet().removeIf(entry -> entry.getValue() < tick);
    }

    private static boolean hasIndependentFlight(ServerPlayer player) {
        return player.isCreative() || player.isSpectator() ||
                player.getItemBySlot(EquipmentSlot.CHEST).is(CosmicItems.SANGUINE_WARPTECH_CHESTPLATE.get());
    }

    private static void grant(ServerPlayer player) {
        if (player.getAbilities().mayfly) return;
        player.getAbilities().mayfly = true;
        player.connection.send(new ClientboundPlayerAbilitiesPacket(player.getAbilities()));
    }

    private static void revoke(ServerPlayer player) {
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        if (!player.onGround()) {
            player.getPersistentData().putBoolean(IMMUNITY_KEY, true);
            player.fallDistance = 0.0f;
        }
        player.connection.send(new ClientboundPlayerAbilitiesPacket(player.getAbilities()));
    }

    private static void clearLandingProtection(ServerPlayer player) {
        player.getPersistentData().remove(IMMUNITY_KEY);
    }

    private static final class FlightState {

        private final Map<GlobalPos, Integer> sources = new HashMap<>();
        private boolean covered;
        private boolean ownsFlight;
    }
}
