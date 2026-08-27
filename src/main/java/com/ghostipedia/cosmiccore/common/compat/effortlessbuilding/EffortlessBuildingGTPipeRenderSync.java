package com.ghostipedia.cosmiccore.common.compat.effortlessbuilding;

import com.gregtechceu.gtceu.api.pipenet.IPipeNode;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class EffortlessBuildingGTPipeRenderSync {

    private static final Map<MinecraftServer, Map<ResourceKey<Level>, Set<BlockPos>>> PENDING = new WeakHashMap<>();

    private EffortlessBuildingGTPipeRenderSync() {}

    public static void schedule(ServerLevel level, Set<IPipeNode<?, ?>> pipes) {
        Set<BlockPos> positions = PENDING.computeIfAbsent(level.getServer(), ignored -> new LinkedHashMap<>())
                .computeIfAbsent(level.dimension(), ignored -> new LinkedHashSet<>());
        for (IPipeNode<?, ?> pipe : pipes) positions.add(pipe.self().getBlockPos().immutable());
    }

    public static void flush(MinecraftServer server) {
        Map<ResourceKey<Level>, Set<BlockPos>> levels = PENDING.remove(server);
        if (levels == null) return;
        for (Map.Entry<ResourceKey<Level>, Set<BlockPos>> entry : levels.entrySet()) {
            ServerLevel level = server.getLevel(entry.getKey());
            if (level == null) continue;
            for (BlockPos pos : entry.getValue()) synchronize(level, pos);
        }
    }

    private static void synchronize(ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) return;
        if (!(level.getBlockEntity(pos) instanceof IPipeNode<?, ?> pipe)) return;
        pipe.getSyncDataHolder().resyncAllFields();
        var state = level.getBlockState(pos);
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        pipe.scheduleRenderUpdate();
    }
}
