package com.ghostipedia.cosmiccore.common.deployment;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public final class LeylineFabricationLibrary extends SavedData {

    private final Map<UUID, LeylinePrefab> prefabs = new LinkedHashMap<>();
    private final List<CompoundTag> unavailable = new ArrayList<>();

    public static LeylineFabricationLibrary get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                new Factory<>(LeylineFabricationLibrary::new, LeylineFabricationLibrary::load, null),
                "cosmiccore_leyline_library");
    }

    public void add(LeylinePrefab prefab) {
        if (prefabs.putIfAbsent(prefab.id(), prefab) == null) setDirty();
    }

    public LeylinePrefab find(UUID id) {
        return prefabs.get(id);
    }

    public Collection<LeylinePrefab> designs() {
        return List.copyOf(prefabs.values());
    }

    private static LeylineFabricationLibrary load(CompoundTag tag, HolderLookup.Provider registries) {
        var library = new LeylineFabricationLibrary();
        var list = tag.getList("designs", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            try {
                var prefab = LeylinePrefab.load(list.getCompound(i));
                library.prefabs.put(prefab.id(), prefab);
            } catch (RuntimeException exception) {
                library.unavailable.add(list.getCompound(i).copy());
                com.ghostipedia.cosmiccore.CosmicCore.LOGGER.warn("Retaining unavailable leyline library entry {}", i,
                        exception);
            }
        }
        return library;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        var list = new ListTag();
        prefabs.values().forEach(prefab -> list.add(prefab.save()));
        unavailable.forEach(entry -> list.add(entry.copy()));
        tag.put("designs", list);
        return tag;
    }
}
