package com.ghostipedia.cosmiccore.api.data.souls;

import com.ghostipedia.cosmiccore.CosmicCore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class SoulNetworkSavedData extends SavedData {

    private static final String DATA_NAME = CosmicCore.MOD_ID + "_soul_network_data";
    private static final String SOUL_NETWORK_MAPPING = "soul_network_mapping";
    private static final String SOUL_NETWORK_UUID = "soul_network_uuid";
    private static final String SOUL_NETWORK_DATA = "soul_network_data";

    public static final HashMap<UUID, SoulNetwork> SoulNetworkMapping = new HashMap<>(20, 0.9f);

    public static SoulNetworkSavedData getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(SoulNetworkSavedData::new, SoulNetworkSavedData::new, DATA_NAME);
    }

    public static SoulNetwork getSoulNetwork(UUID owner) {
        return SoulNetworkMapping.computeIfAbsent(owner, id -> new SoulNetwork());
    }

    private SoulNetworkSavedData() {}

    private SoulNetworkSavedData(CompoundTag nbt) {
        var list = nbt.getList(SOUL_NETWORK_MAPPING, CompoundTag.TAG_COMPOUND);
        for (Tag tag : list) {
            if (tag instanceof CompoundTag compoundTag) {
                var uuid = UUID.fromString(compoundTag.getString(SOUL_NETWORK_UUID));
                var data = new SoulNetwork();
                data.deserializeNBT(compoundTag.getCompound(SOUL_NETWORK_DATA));
                SoulNetworkMapping.put(uuid, data);
            }
        }
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag) {
        var soulNetworkDataList = new ListTag();
        for (var entry : SoulNetworkMapping.entrySet()) {
            var tag = new CompoundTag();
            tag.putString(SOUL_NETWORK_UUID, entry.getKey().toString());
            tag.put(SOUL_NETWORK_DATA, entry.getValue().serializeNBT());
            soulNetworkDataList.add(tag);
        }
        compoundTag.put(SOUL_NETWORK_MAPPING, soulNetworkDataList);
        return compoundTag;
    }
}
