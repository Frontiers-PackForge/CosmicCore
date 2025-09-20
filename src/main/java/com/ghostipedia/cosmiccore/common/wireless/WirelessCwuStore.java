package com.ghostipedia.cosmiccore.common.wireless;

import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationReceiver;

import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.ghostipedia.cosmiccore.common.wireless.GlobalWirelessVariableStorage.GlobalWirelessCwu;

public class WirelessCwuStore implements IOpticalComputationProvider {

    private final Set<IOpticalComputationProvider> providers = new HashSet<>();

    public void clearData() {
        providers.clear();
    }

    public void addTransmitters(Set<IOpticalComputationProvider> data) {
        providers.addAll(data);
    }

    public void removeTransmitters(Set<IOpticalComputationProvider> data) {
        data.forEach(providers::remove);
    }

    public List<IOpticalComputationProvider> getProviders() {
        return providers.stream().toList();
    }

    @Override
    public int requestCWUt(int cwut, boolean simulate, @NotNull Collection<IOpticalComputationProvider> seen) {
        if (seen.contains(this)) return 0;
        // The max CWU/t that this Network Switch can provide, combining all its inputs.
        seen.add(this);
        Collection<IOpticalComputationProvider> bridgeSeen = new ArrayList<>(seen);
        int allocatedCWUt = 0;
        for (var provider : providers) {
            if (!provider.canBridge(bridgeSeen)) continue;
            int allocated = provider.requestCWUt(cwut, simulate, seen);
            allocatedCWUt += allocated;
            cwut -= allocated;
            if (cwut == 0) break;
        }
        return allocatedCWUt;
    }

    @Override
    public int getMaxCWUt(@NotNull Collection<IOpticalComputationProvider> seen) {
        if (seen.contains(this)) return 0;
        // The max CWU/t that this Network Switch can provide, combining all its inputs.
        seen.add(this);
        Collection<IOpticalComputationProvider> bridgeSeen = new ArrayList<>(seen);
        int maximumCWUt = 0;
        for (var provider : providers) {
            if (!provider.canBridge(bridgeSeen)) continue;
            maximumCWUt += provider.getMaxCWUt(seen);
        }
        return maximumCWUt;
    }

    @Override
    public boolean canBridge(@NotNull Collection<IOpticalComputationProvider> collection) {
        return false;
    }

    public static WirelessCwuStore getWirelessCwuStore(UUID uuid) {
        if (GlobalWirelessCwu.get(uuid) == null)
            GlobalWirelessCwu.put(uuid, new WirelessCwuStore());
        return GlobalWirelessCwu.get(uuid);
    }

    public static void addHatches(UUID uuid, List<IOpticalComputationReceiver> receivers) {
        var dataStore = getWirelessCwuStore(uuid);
        var providers = receivers.stream().map(IOpticalComputationReceiver::getComputationProvider).toList();
        dataStore.addTransmitters(new HashSet<>(providers));
        GlobalWirelessCwu.put(uuid, dataStore);
    }

    public static void removeHatches(UUID uuid, List<IOpticalComputationReceiver> receivers) {
        var dataStore = getWirelessCwuStore(uuid);
        var providers = receivers.stream().map(IOpticalComputationReceiver::getComputationProvider).toList();
        dataStore.removeTransmitters(new HashSet<>(providers));
        GlobalWirelessCwu.put(uuid, dataStore);
    }
}
