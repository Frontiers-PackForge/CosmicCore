package com.ghostipedia.cosmiccore.common.transmission.graph;

import com.ghostipedia.cosmiccore.common.transmission.energy.LoadedPowerTowerTerminalRegistry;
import com.ghostipedia.cosmiccore.common.transmission.me.PowerTowerMERegistry;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class PowerTowerSavedData extends SavedData {

    private static final String DATA_NAME = "cosmiccore_power_towers";
    private final PowerTowerGraph graph;
    private final PowerTowerMERegistry meCircuits;
    private ServerLevel level;
    private final LoadedPowerTowerTerminalRegistry loadedTerminals = new LoadedPowerTowerTerminalRegistry();

    private PowerTowerSavedData() {
        graph = new PowerTowerGraph();
        meCircuits = new PowerTowerMERegistry(this, new CompoundTag());
    }

    private PowerTowerSavedData(CompoundTag tag) {
        graph = PowerTowerGraph.loadFromTag(tag);
        meCircuits = new PowerTowerMERegistry(this, tag.getCompound("MECircuits"));
    }

    public static PowerTowerSavedData getOrCreate(ServerLevel level) {
        var data = level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(PowerTowerSavedData::new,
                PowerTowerSavedData::load), DATA_NAME);
        data.level = level;
        return data;
    }

    public static PowerTowerSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        return new PowerTowerSavedData(tag);
    }

    public PowerTowerGraph graph() {
        return graph;
    }

    public LoadedPowerTowerTerminalRegistry loadedTerminals() {
        return loadedTerminals;
    }

    public void markGraphDirty() {
        setDirty();
        meCircuits.changed();
        if (level != null) com.ghostipedia.cosmiccore.common.transmission.PowerTowerSpanSync.changed(level);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        var result = graph.saveToTag();
        result.put("MECircuits", meCircuits.save());
        return result;
    }

    public PowerTowerMERegistry meCircuits() {
        return meCircuits;
    }
}
