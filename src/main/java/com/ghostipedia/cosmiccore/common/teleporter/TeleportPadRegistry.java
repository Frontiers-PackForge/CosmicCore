package com.ghostipedia.cosmiccore.common.teleporter;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

// Tracks teleport pads that have been placed. Prevents duplicate platform spawning.
public class TeleportPadRegistry extends SavedData {

    private static final String DATA_NAME = "cosmiccore_teleport_pads";

    private final Set<BlockPos> pads = new HashSet<>();

    public TeleportPadRegistry() {
        super();
    }

    // Get the saved data instance for a specific dimension.
    public static TeleportPadRegistry get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                TeleportPadRegistry::load,
                TeleportPadRegistry::new,
                DATA_NAME);
    }

    // Check if a pad exists at the given position.
    public boolean hasPadAt(BlockPos pos) {
        return pads.contains(pos);
    }

    // Register a new pad at the given position.
    public void registerPad(BlockPos pos) {
        if (pads.add(pos)) {
            setDirty();
        }
    }

    // Remove a pad registration (like if it gets broken)
    public void removePad(BlockPos pos) {
        if (pads.remove(pos)) {
            setDirty();
        }
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag padsList = new ListTag();

        for (BlockPos pos : pads) {
            CompoundTag padTag = new CompoundTag();
            padTag.putLong("Pos", pos.asLong());
            padsList.add(padTag);
        }

        tag.put("Pads", padsList);
        return tag;
    }

    public static TeleportPadRegistry load(CompoundTag tag) {
        TeleportPadRegistry registry = new TeleportPadRegistry();

        ListTag padsList = tag.getList("Pads", Tag.TAG_COMPOUND);
        for (Tag padTagRaw : padsList) {
            CompoundTag padTag = (CompoundTag) padTagRaw;
            BlockPos pos = BlockPos.of(padTag.getLong("Pos"));
            registry.pads.add(pos);
        }

        return registry;
    }
}
