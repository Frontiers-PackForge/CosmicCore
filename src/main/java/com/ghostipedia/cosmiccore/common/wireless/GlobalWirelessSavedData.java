package com.ghostipedia.cosmiccore.common.wireless;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import org.jetbrains.annotations.NotNull;

public class GlobalWirelessSavedData extends SavedData {

    private final ServerLevel serverLevel;

    public static GlobalWirelessSavedData getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(() -> new GlobalWirelessSavedData(serverLevel),
                        (tag, provider) -> new GlobalWirelessSavedData(serverLevel, tag)),
                "gtceu_global_wireless");
    }

    private GlobalWirelessSavedData(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
    }

    private GlobalWirelessSavedData(ServerLevel serverLevel, CompoundTag tag) {
        this(serverLevel);
    }

    @NotNull
    @Override
    public CompoundTag save(@NotNull CompoundTag compound, @NotNull HolderLookup.Provider provider) {
        return compound;
    }
}
