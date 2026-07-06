package com.ghostipedia.cosmiccore.common.data.worldgen.field;

import com.ghostipedia.cosmiccore.client.map.RevealedField;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FieldDiscoveryData extends SavedData {

    private static final String NAME = "cosmiccore_field_discovery";

    // teamKey -> dimension id -> (packed field position -> revealed field). Dedup is by field CORE position, so a
    // field discovered by any tool (dowsing rod, survey scanner) counts once regardless of the reveal tier.
    private final Map<String, Map<String, LinkedHashMap<Long, RevealedField>>> byTeam = new HashMap<>();

    public static FieldDiscoveryData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(FieldDiscoveryData::new, FieldDiscoveryData::load), NAME);
    }

    private FieldDiscoveryData() {}

    private static long posKey(RevealedField field) {
        return ((long) field.x() << 32) | (field.z() & 0xFFFFFFFFL);
    }

    public List<RevealedField> addAll(String teamKey, ResourceLocation dimension, List<RevealedField> fields) {
        LinkedHashMap<Long, RevealedField> map = byTeam
                .computeIfAbsent(teamKey, key -> new HashMap<>())
                .computeIfAbsent(dimension.toString(), key -> new LinkedHashMap<>());
        List<RevealedField> added = new ArrayList<>();
        for (RevealedField field : fields) {
            if (map.putIfAbsent(posKey(field), field) == null) added.add(field);
        }
        if (!added.isEmpty()) setDirty();
        return added;
    }

    public List<RevealedField> get(String teamKey, ResourceLocation dimension) {
        Map<String, LinkedHashMap<Long, RevealedField>> dims = byTeam.get(teamKey);
        if (dims == null) return List.of();
        LinkedHashMap<Long, RevealedField> map = dims.get(dimension.toString());
        return map == null ? List.of() : new ArrayList<>(map.values());
    }

    public Set<String> dimensionsFor(String teamKey) {
        Map<String, LinkedHashMap<Long, RevealedField>> dims = byTeam.get(teamKey);
        return dims == null ? Set.of() : new HashSet<>(dims.keySet());
    }

    @NotNull
    @Override
    public CompoundTag save(@NotNull CompoundTag compound, @NotNull HolderLookup.Provider provider) {
        CompoundTag teams = new CompoundTag();
        byTeam.forEach((teamKey, dims) -> {
            CompoundTag dimsTag = new CompoundTag();
            dims.forEach((dimension, map) -> {
                ListTag list = new ListTag();
                for (RevealedField field : map.values()) {
                    list.add(field.toTag());
                }
                dimsTag.put(dimension, list);
            });
            teams.put(teamKey, dimsTag);
        });
        compound.put("teams", teams);
        return compound;
    }

    private static FieldDiscoveryData load(CompoundTag tag, HolderLookup.Provider provider) {
        FieldDiscoveryData data = new FieldDiscoveryData();
        CompoundTag teams = tag.getCompound("teams");
        for (String teamKey : teams.getAllKeys()) {
            CompoundTag dimsTag = teams.getCompound(teamKey);
            Map<String, LinkedHashMap<Long, RevealedField>> dims = new HashMap<>();
            for (String dimension : dimsTag.getAllKeys()) {
                ListTag list = dimsTag.getList(dimension, Tag.TAG_COMPOUND);
                LinkedHashMap<Long, RevealedField> map = new LinkedHashMap<>();
                for (int i = 0; i < list.size(); i++) {
                    RevealedField field = RevealedField.fromTag(list.getCompound(i));
                    map.putIfAbsent(posKey(field), field);
                }
                dims.put(dimension, map);
            }
            data.byTeam.put(teamKey, dims);
        }
        return data;
    }
}
