package com.ghostipedia.cosmiccore.common.wireless;

import com.gregtechceu.gtceu.api.capability.IDataAccessHatch;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.ghostipedia.cosmiccore.common.wireless.GlobalWirelessVariableStorage.GlobalWirelessDataSticks;

public class WirelessDataStore {

    private final Set<IDataAccessHatch> transmitters = new HashSet<>();

    public void clearData() {
        transmitters.clear();
    }

    public void addTransmitters(List<IDataAccessHatch> data) {
        transmitters.addAll(data);
    }

    public void removeTransmitters(List<IDataAccessHatch> data) {
        data.forEach(transmitters::remove);
    }

    public List<IDataAccessHatch> getTransmitters() {
        return transmitters.stream().toList();
    }

    public boolean isRecipeAvailable(@NotNull GTRecipe recipe, @NotNull Collection<IDataAccessHatch> seen) {
        return transmitters.stream().anyMatch(t -> t.isRecipeAvailable(recipe, seen));
    }

    public static WirelessDataStore getWirelessDataStore(UUID uuid) {
        if (GlobalWirelessDataSticks.get(uuid) == null)
            GlobalWirelessDataSticks.put(uuid, new WirelessDataStore());
        return GlobalWirelessDataSticks.get(uuid);
    }

    public static void addHatches(UUID uuid, List<IDataAccessHatch> hatches) {
        var dataStore = getWirelessDataStore(uuid);
        dataStore.addTransmitters(hatches);
        GlobalWirelessDataSticks.put(uuid, dataStore);
    }

    public static void removeHatches(UUID uuid, List<IDataAccessHatch> hatches) {
        var dataStore = getWirelessDataStore(uuid);
        dataStore.removeTransmitters(hatches);
        GlobalWirelessDataSticks.put(uuid, dataStore);
    }
}
