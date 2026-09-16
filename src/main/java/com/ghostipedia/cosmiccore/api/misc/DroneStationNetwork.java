package com.ghostipedia.cosmiccore.api.misc;

import com.ghostipedia.cosmiccore.api.machine.multiblock.DroneStationMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.WeakHashMap;

public final class DroneStationNetwork {

    private static final WeakHashMap<ServerLevel, NavigableMap<Long, WeakReference<DroneStationMachine>>> STATIONS = new WeakHashMap<>();

    private DroneStationNetwork() {}

    public static synchronized void register(DroneStationMachine station) {
        if (!(station.getLevel() instanceof ServerLevel level)) return;
        stations(level).put(station.getBlockPos().asLong(), new WeakReference<>(station));
    }

    public static synchronized void unregister(DroneStationMachine station) {
        if (!(station.getLevel() instanceof ServerLevel level)) return;
        NavigableMap<Long, WeakReference<DroneStationMachine>> stations = STATIONS.get(level);
        if (stations == null) return;
        stations.remove(station.getBlockPos().asLong());
        prune(level, stations);
    }

    public static synchronized @Nullable DroneStationMachine nearestActive(ServerLevel level, BlockPos target) {
        NavigableMap<Long, WeakReference<DroneStationMachine>> stations = stations(level);
        DroneStationMachine nearest = null;
        double nearestDistance = Double.POSITIVE_INFINITY;
        for (Iterator<WeakReference<DroneStationMachine>> iterator = stations.values().iterator(); iterator
                .hasNext();) {
            DroneStationMachine candidate = iterator.next().get();
            if (candidate == null || candidate.isRemoved()) {
                iterator.remove();
                continue;
            }
            if (!candidate.canServe(target)) continue;
            double distance = DroneStationSpace.squaredDistance(level, candidate.getBlockPos(), target);
            if (nearest == null || DroneStationServiceLogic.isBetterStation(distance,
                    candidate.getBlockPos().getX(), candidate.getBlockPos().getY(), candidate.getBlockPos().getZ(),
                    nearestDistance, nearest.getBlockPos().getX(), nearest.getBlockPos().getY(),
                    nearest.getBlockPos().getZ())) {
                nearest = candidate;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private static NavigableMap<Long, WeakReference<DroneStationMachine>> stations(ServerLevel level) {
        return STATIONS.computeIfAbsent(level, ignored -> new TreeMap<>());
    }

    private static void prune(ServerLevel level,
                              NavigableMap<Long, WeakReference<DroneStationMachine>> stations) {
        stations.values().removeIf(reference -> {
            DroneStationMachine station = reference.get();
            return station == null || station.isRemoved();
        });
        if (stations.isEmpty()) STATIONS.remove(level);
    }
}
