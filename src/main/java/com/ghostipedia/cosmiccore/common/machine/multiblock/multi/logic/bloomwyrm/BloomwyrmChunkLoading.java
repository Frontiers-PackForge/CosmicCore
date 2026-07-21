package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.common.world.chunk.TicketHelper;

import java.util.HashSet;
import java.util.Set;

public final class BloomwyrmChunkLoading {

    private static final TicketController TICKETS = new TicketController(
            CosmicCore.id("bloomwyrm_campus"),
            BloomwyrmChunkLoading::discardStaleTickets);

    private BloomwyrmChunkLoading() {}

    public static void register(RegisterTicketControllersEvent event) {
        event.register(TICKETS);
    }

    public static boolean isExternallyForced(ServerLevel level, BlockPos controllerPos) {
        ForcedChunksSavedData data = level.getDataStorage().get(ForcedChunksSavedData.factory(), "chunks");
        if (data == null) return false;

        long chunk = new ChunkPos(controllerPos).toLong();
        if (data.getChunks().contains(chunk)) return true;

        boolean blockTicket = data.getBlockForcedChunks().getChunks().values().stream()
                .anyMatch(chunks -> chunks.contains(chunk));
        if (blockTicket) return true;

        return data.getBlockForcedChunks().getTickingChunks().values().stream()
                .anyMatch(chunks -> chunks.contains(chunk)) ||
                data.getEntityForcedChunks().getChunks().values().stream()
                        .anyMatch(chunks -> chunks.contains(chunk)) ||
                data.getEntityForcedChunks().getTickingChunks().values().stream()
                        .anyMatch(chunks -> chunks.contains(chunk));
    }

    public static void update(
                              ServerLevel level,
                              BlockPos owner,
                              Set<Long> currentChunks,
                              Set<Long> requiredChunks) {
        Set<Long> removed = new HashSet<>(currentChunks);
        removed.removeAll(requiredChunks);
        for (long chunk : removed) {
            ChunkPos pos = new ChunkPos(chunk);
            TICKETS.forceChunk(level, owner, pos.x, pos.z, false, true);
        }

        Set<Long> added = new HashSet<>(requiredChunks);
        added.removeAll(currentChunks);
        for (long chunk : added) {
            ChunkPos pos = new ChunkPos(chunk);
            TICKETS.forceChunk(level, owner, pos.x, pos.z, true, true);
        }

        currentChunks.clear();
        currentChunks.addAll(requiredChunks);
    }

    public static void release(ServerLevel level, BlockPos owner, Set<Long> currentChunks) {
        update(level, owner, currentChunks, Set.of());
    }

    private static void discardStaleTickets(ServerLevel level, TicketHelper helper) {
        for (BlockPos owner : helper.getBlockTickets().keySet()) {
            helper.removeAllTickets(owner);
        }
    }
}
