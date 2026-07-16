package com.ghostipedia.cosmiccore.mixin.gtfix.xaerominimap;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.integration.map.xaeros.XaeroWaypointHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.WaypointPurpose;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Mixin(value = XaeroWaypointHandler.class, remap = false)
public abstract class XaeroWaypointHandlerMixin {

    @Unique
    private final Map<String, Waypoint> cosmiccore$nativeWaypoints = new HashMap<>();

    @Unique
    private final Map<String, MinimapWorld> cosmiccore$nativeWorlds = new HashMap<>();

    @Unique
    private final Map<String, WaypointSet> cosmiccore$nativeSets = new HashMap<>();

    @Inject(
            method = "setWaypoint(Ljava/lang/String;Ljava/lang/String;ILnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/BlockPos;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void cosmiccore$setNativeWaypoint(String key, String name, int color, ResourceKey<Level> dimension,
                                              BlockPos pos, CallbackInfo ci) {
        MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
        if (session == null) return;

        session.getWorldStateUpdater().update();
        MinimapWorld world = cosmiccore$resolveWorld(session, dimension);
        if (world == null) return;

        WaypointSet waypointSet = world.getCurrentWaypointSet();
        if (waypointSet == null) return;

        Waypoint waypoint = cosmiccore$nativeWaypoints.remove(key);
        MinimapWorld previousWorld = cosmiccore$nativeWorlds.remove(key);
        WaypointSet previousSet = cosmiccore$nativeSets.remove(key);
        if (waypoint != null && previousWorld != null && previousSet != null &&
                cosmiccore$contains(previousSet, waypoint)) {
            if (previousSet != waypointSet) {
                previousSet.remove(waypoint);
                cosmiccore$save(session, previousWorld);
                waypoint = null;
            }
        } else {
            waypoint = null;
        }

        if (waypoint == null) {
            waypoint = cosmiccore$find(waypointSet, name, pos);
        }
        if (waypoint == null) {
            String symbol = name.isEmpty() ? "?" : name.substring(0, 1);
            waypoint = new Waypoint(pos.getX(), pos.getY(), pos.getZ(), name, symbol,
                    cosmiccore$nearestColor(color), WaypointPurpose.NORMAL);
            waypointSet.add(waypoint, true);
        } else {
            waypoint.setX(pos.getX());
            waypoint.setY(pos.getY());
            waypoint.setZ(pos.getZ());
            waypoint.setName(name);
            waypoint.setInitials(name.isEmpty() ? "?" : name.substring(0, 1));
            waypoint.setWaypointColor(cosmiccore$nearestColor(color));
        }

        cosmiccore$nativeWaypoints.put(key, waypoint);
        cosmiccore$nativeWorlds.put(key, world);
        cosmiccore$nativeSets.put(key, waypointSet);
        cosmiccore$save(session, world);
        ci.cancel();
    }

    @Inject(method = "removeWaypoint(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$removeNativeWaypoint(String key, CallbackInfo ci) {
        Waypoint waypoint = cosmiccore$nativeWaypoints.remove(key);
        MinimapWorld world = cosmiccore$nativeWorlds.remove(key);
        WaypointSet waypointSet = cosmiccore$nativeSets.remove(key);
        if (waypoint == null || world == null || waypointSet == null) return;

        waypointSet.remove(waypoint);
        MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
        if (session != null) {
            cosmiccore$save(session, world);
        }
        ci.cancel();
    }

    @Unique
    private static MinimapWorld cosmiccore$resolveWorld(MinimapSession session, ResourceKey<Level> dimension) {
        MinimapWorld world = session.getWorldManager().getCurrentWorld();
        if (cosmiccore$matchesDimension(world, dimension)) return world;

        world = session.getWorldManager().getAutoWorld();
        return cosmiccore$matchesDimension(world, dimension) ? world : null;
    }

    @Unique
    private static boolean cosmiccore$matchesDimension(MinimapWorld world, ResourceKey<Level> dimension) {
        return world != null && (dimension == null || world.getDimId() == null || dimension.equals(world.getDimId()));
    }

    @Unique
    private static boolean cosmiccore$contains(WaypointSet waypointSet, Waypoint waypoint) {
        for (Waypoint candidate : waypointSet.getWaypoints()) {
            if (candidate == waypoint) return true;
        }
        return false;
    }

    @Unique
    private static Waypoint cosmiccore$find(WaypointSet waypointSet, String name, BlockPos pos) {
        for (Waypoint waypoint : waypointSet.getWaypoints()) {
            if (waypoint.getPurpose() == WaypointPurpose.NORMAL && waypoint.getX() == pos.getX() &&
                    waypoint.getY() == pos.getY() && waypoint.getZ() == pos.getZ() &&
                    waypoint.getName().equals(name)) {
                return waypoint;
            }
        }
        return null;
    }

    @Unique
    private static WaypointColor cosmiccore$nearestColor(int color) {
        int red = color >> 16 & 0xFF;
        int green = color >> 8 & 0xFF;
        int blue = color & 0xFF;
        WaypointColor nearest = WaypointColor.WHITE;
        int nearestDistance = Integer.MAX_VALUE;
        for (WaypointColor candidate : WaypointColor.values()) {
            int candidateColor = candidate.getHex();
            int redDelta = red - (candidateColor >> 16 & 0xFF);
            int greenDelta = green - (candidateColor >> 8 & 0xFF);
            int blueDelta = blue - (candidateColor & 0xFF);
            int distance = redDelta * redDelta + greenDelta * greenDelta + blueDelta * blueDelta;
            if (distance < nearestDistance) {
                nearest = candidate;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    @Unique
    private static void cosmiccore$save(MinimapSession session, MinimapWorld world) {
        try {
            session.getWorldManagerIO().saveWorld(world);
        } catch (IOException exception) {
            CosmicCore.LOGGER.error("Failed to save Xaero waypoint world {}", world.getFullPath(), exception);
        }
    }
}
