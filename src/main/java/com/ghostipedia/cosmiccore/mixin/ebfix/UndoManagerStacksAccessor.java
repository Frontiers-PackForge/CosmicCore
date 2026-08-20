package com.ghostipedia.cosmiccore.mixin.ebfix;

import neoforge.nl.requios.effortlessbuilding.utilities.UndoManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Deque;
import java.util.Map;
import java.util.UUID;

@Mixin(value = UndoManager.class, remap = false)
public interface UndoManagerStacksAccessor {

    @Accessor("undoStacks")
    static Map<UUID, Deque<UndoManager.UndoEntry>> cosmiccore$getUndoStacks() {
        throw new AssertionError();
    }

    @Accessor("redoStacks")
    static Map<UUID, Deque<UndoManager.UndoEntry>> cosmiccore$getRedoStacks() {
        throw new AssertionError();
    }
}
