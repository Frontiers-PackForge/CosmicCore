package com.ghostipedia.cosmiccore.mixin.ebfix;

import com.ghostipedia.cosmiccore.common.compat.effortlessbuilding.EffortlessBuildingOperationExecutor;
import com.ghostipedia.cosmiccore.common.compat.effortlessbuilding.EffortlessBuildingOperationMetadata;
import com.ghostipedia.cosmiccore.common.compat.effortlessbuilding.EffortlessBuildingUndoEntryExtension;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import neoforge.nl.requios.effortlessbuilding.utilities.UndoManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;

@Mixin(value = UndoManager.class, remap = false)
public abstract class UndoManagerOperationMetadataMixin {

    @Inject(method = "undo", at = @At("HEAD"), cancellable = true)
    private static void cosmiccore$undoOperation(
                                                 ServerPlayer player,
                                                 CallbackInfoReturnable<Integer> cir) {
        cosmiccore$execute(
                player, UndoManagerStacksAccessor.cosmiccore$getUndoStacks(),
                UndoManagerStacksAccessor.cosmiccore$getRedoStacks(), true, cir);
    }

    @Inject(method = "redo", at = @At("HEAD"), cancellable = true)
    private static void cosmiccore$redoOperation(
                                                 ServerPlayer player,
                                                 CallbackInfoReturnable<Integer> cir) {
        cosmiccore$execute(
                player, UndoManagerStacksAccessor.cosmiccore$getRedoStacks(),
                UndoManagerStacksAccessor.cosmiccore$getUndoStacks(), false, cir);
    }

    @Unique
    private static void cosmiccore$execute(
                                           ServerPlayer player,
                                           Map<UUID, Deque<UndoManager.UndoEntry>> sourceStacks,
                                           Map<UUID, Deque<UndoManager.UndoEntry>> destinationStacks, boolean undo,
                                           CallbackInfoReturnable<Integer> cir) {
        Deque<UndoManager.UndoEntry> source = sourceStacks.get(player.getUUID());
        if (source == null || source.isEmpty()) return;
        UndoManager.UndoEntry entry = source.getFirst();
        EffortlessBuildingUndoEntryExtension extension = (EffortlessBuildingUndoEntryExtension) (Object) entry;
        EffortlessBuildingOperationMetadata metadata = extension.cosmiccore$getOperationMetadata(); // Screams about NPE
                                                                                                    // warning, should
                                                                                                    // be fine, I think
                                                                                                    // it's just Mixin
                                                                                                    // being dumb!
        if (metadata == null) return;

        source.pop();
        ServerLevel level = player.server.getLevel(entry.dimension());
        if (level == null) {
            cir.setReturnValue(-1);
            return;
        }
        EffortlessBuildingOperationExecutor.Result result;
        try {
            result = undo ? EffortlessBuildingOperationExecutor.undo(player, level, metadata) :
                    EffortlessBuildingOperationExecutor.redo(player, level, metadata);
        } catch (RuntimeException exception) {
            source.push(entry);
            throw exception;
        }
        Deque<UndoManager.UndoEntry> destination = destinationStacks.computeIfAbsent(
                player.getUUID(), ignored -> new ArrayDeque<>());
        destination.push(entry);
        if (destination.size() > 50) ((ArrayDeque<UndoManager.UndoEntry>) destination).removeLast();
        cir.setReturnValue(result.changed());
    }
}
