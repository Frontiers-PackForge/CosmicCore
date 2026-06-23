package com.ghostipedia.cosmiccore.client.map;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public final class RevealedFields {

    public static final RevealedFields INSTANCE = new RevealedFields();

    private final Map<ResourceKey<Level>, Map<Long, RevealedField>> byDim = new HashMap<>();
    private final Map<ResourceKey<Level>, Set<Long>> depleted = new HashMap<>();

    private RevealedFields() {}

    public void put(ResourceKey<Level> dimension, RevealedField field) {
        Map<Long, RevealedField> dim = byDim.computeIfAbsent(dimension, k -> new HashMap<>());
        long key = key(field.x(), field.z());
        RevealedField existing = dim.get(key);
        if (existing == null || field.tier() >= existing.tier()) {
            dim.put(key, field);
        }
    }

    public Collection<RevealedField> forDim(ResourceKey<Level> dimension) {
        Map<Long, RevealedField> dim = byDim.get(dimension);
        return dim == null ? List.of() : new ArrayList<>(dim.values());
    }

    public boolean isDepleted(ResourceKey<Level> dimension, int x, int z) {
        Set<Long> set = depleted.get(dimension);
        return set != null && set.contains(key(x, z));
    }

    public void toggleDepleted(ResourceKey<Level> dimension, int x, int z) {
        Set<Long> set = depleted.computeIfAbsent(dimension, k -> new HashSet<>());
        long key = key(x, z);
        if (!set.remove(key)) {
            set.add(key);
        }
    }

    public void clearAll() {
        byDim.clear();
        depleted.clear();
    }

    public CompoundTag toNbt() {
        CompoundTag root = new CompoundTag();
        ListTag dims = new ListTag();
        for (Map.Entry<ResourceKey<Level>, Map<Long, RevealedField>> entry : byDim.entrySet()) {
            CompoundTag dimTag = new CompoundTag();
            dimTag.putString("dim", entry.getKey().location().toString());
            ListTag fields = new ListTag();
            for (RevealedField field : entry.getValue().values()) {
                fields.add(field.toTag());
            }
            dimTag.put("fields", fields);
            Set<Long> dep = depleted.get(entry.getKey());
            if (dep != null && !dep.isEmpty()) {
                dimTag.putLongArray("depleted", dep.stream().mapToLong(Long::longValue).toArray());
            }
            dims.add(dimTag);
        }
        root.put("dims", dims);
        return root;
    }

    public void fromNbt(CompoundTag root) {
        clearAll();
        ListTag dims = root.getList("dims", Tag.TAG_COMPOUND);
        for (int i = 0; i < dims.size(); i++) {
            CompoundTag dimTag = dims.getCompound(i);
            ResourceLocation loc = ResourceLocation.tryParse(dimTag.getString("dim"));
            if (loc == null) continue;
            ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, loc);
            ListTag fields = dimTag.getList("fields", Tag.TAG_COMPOUND);
            for (int j = 0; j < fields.size(); j++) {
                put(dimension, RevealedField.fromTag(fields.getCompound(j)));
            }
            if (dimTag.contains("depleted")) {
                Set<Long> dep = depleted.computeIfAbsent(dimension, k -> new HashSet<>());
                for (long value : dimTag.getLongArray("depleted")) {
                    dep.add(value);
                }
            }
        }
    }

    private static long key(int x, int z) {
        return (x & 0xFFFFFFFFL) | ((long) z << 32);
    }
}
