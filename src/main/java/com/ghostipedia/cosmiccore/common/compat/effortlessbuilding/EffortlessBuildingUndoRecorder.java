package com.ghostipedia.cosmiccore.common.compat.effortlessbuilding;

import com.ghostipedia.cosmiccore.mixin.ebfix.UndoManagerStacksAccessor;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import neoforge.nl.requios.effortlessbuilding.utilities.UndoManager;

import java.util.Deque;
import java.util.Map;

public final class EffortlessBuildingUndoRecorder {

    private EffortlessBuildingUndoRecorder() {}

    public static void record(
                              ServerPlayer player, ResourceKey<Level> dimension,
                              Map<BlockPos, UndoManager.BlockChange> changes,
                              EffortlessBuildingOperationMetadata metadata) {
        UndoManager.recordOperation(player, dimension, changes);
        Deque<UndoManager.UndoEntry> stack = UndoManagerStacksAccessor.cosmiccore$getUndoStacks()
                .get(player.getUUID());
        if (stack == null || stack.isEmpty() ||
                !((Object) stack.peek() instanceof EffortlessBuildingUndoEntryExtension extension)) {
            throw new IllegalStateException("Effortless Building undo metadata attachment failed");
        }
        extension.cosmiccore$setOperationMetadata(metadata);
    }
}
