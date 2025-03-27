package com.ghostipedia.cosmiccore.common.wireless;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import org.jetbrains.annotations.NotNull;

public class GlobalWirelessSavedData extends SavedData {

    private final ServerLevel serverLevel;

    public static GlobalWirelessSavedData getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(tag -> new GlobalWirelessSavedData(serverLevel, tag),
                () -> new GlobalWirelessSavedData(serverLevel), "gtceu_global_wireless");
    }

    private GlobalWirelessSavedData(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
    }

    private GlobalWirelessSavedData(ServerLevel serverLevel, CompoundTag tag) {
        this(serverLevel);
    }

    @NotNull
    @Override
    public CompoundTag save(@NotNull CompoundTag compound) {
        return compound;
    }
}
