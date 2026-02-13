package com.ghostipedia.cosmiccore.api.data.savedData;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class StarLadderSavedData extends SavedData {

    private static final String DATA_NAME = CosmicCore.MOD_ID + "_star_ladder_data";
    private static final String ESTABLISHED_KEY = "established_teams";
    private static final String UUID_KEY = "uuid";

    private final Set<UUID> establishedTeams = new HashSet<>();

    public StarLadderSavedData() {}

    public StarLadderSavedData(CompoundTag tag) {
        var list = tag.getList(ESTABLISHED_KEY, Tag.TAG_COMPOUND);
        for (Tag entry : list) {
            if (entry instanceof CompoundTag compound) {
                establishedTeams.add(UUID.fromString(compound.getString(UUID_KEY)));
            }
        }
    }

    public static StarLadderSavedData getOrCreate(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
                StarLadderSavedData::new,
                StarLadderSavedData::new,
                DATA_NAME);
    }

    public static StarLadderSavedData getOrCreate(ServerLevel level) {
        return getOrCreate(level.getServer());
    }

    public boolean isEstablished(UUID teamId) {
        return establishedTeams.contains(teamId);
    }

    public void setEstablished(UUID teamId) {
        if (establishedTeams.add(teamId)) {
            setDirty();
        }
    }

    public boolean resetEstablished(UUID teamId) {
        if (establishedTeams.remove(teamId)) {
            setDirty();
            return true;
        }
        return false;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        var list = new ListTag();
        for (UUID uuid : establishedTeams) {
            var entry = new CompoundTag();
            entry.putString(UUID_KEY, uuid.toString());
            list.add(entry);
        }
        tag.put(ESTABLISHED_KEY, list);
        return tag;
    }
}
