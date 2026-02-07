package com.ghostipedia.cosmiccore.common.wireless;

import com.gregtechceu.gtceu.api.capability.IOpticalComputationHatch;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.ghostipedia.cosmiccore.common.wireless.GlobalWirelessVariableStorage.GlobalWirelessComputation;

public class WirelessComputationStore implements IOpticalComputationProvider {

    private final Set<IOpticalComputationHatch> providers = new ObjectOpenHashSet<>();

    private boolean tickSaturated;
    private long timerCWUt = -1;

    private int lastAllocatedCWUt = 0;
    private int currentAllocatedCWUt = 0;
    private long allocationTimer = -1;

    public void clearProviders() {
        providers.clear();
    }

    public void addProviders(List<IOpticalComputationHatch> hatches) {
        providers.addAll(hatches);
    }

    public void removeProviders(List<IOpticalComputationHatch> hatches) {
        hatches.forEach(providers::remove);
    }

    public Set<IOpticalComputationHatch> getProviders() {
        return Collections.unmodifiableSet(providers);
    }

    public int getProviderCount() {
        return providers.size();
    }

    public int getAllocatedCWUt() {
        return lastAllocatedCWUt;
    }

    @Override
    public int requestCWUt(int cwut, boolean simulate, @NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);

        if (cwut == 0) return 0;

        long currentTime = System.currentTimeMillis() / 50;
        if (timerCWUt == currentTime) {
            if (tickSaturated) {
                return 0;
            }
        } else {
            timerCWUt = currentTime;
            tickSaturated = false;
        }

        if (allocationTimer != currentTime) {
            lastAllocatedCWUt = currentAllocatedCWUt;
            currentAllocatedCWUt = 0;
            allocationTimer = currentTime;
        }

        Collection<IOpticalComputationProvider> bridgeSeen = new ArrayList<>(seen);
        int allocatedCWUt = 0;

        for (var provider : providers) {
            if (!provider.canBridge(bridgeSeen)) continue;
            int allocated = provider.requestCWUt(cwut, simulate, seen);
            allocatedCWUt += allocated;
            cwut -= allocated;
            if (cwut == 0) break;
        }

        if (!simulate) {
            currentAllocatedCWUt += allocatedCWUt;
            if (allocatedCWUt == 0) {
                tickSaturated = true;
            }
        }

        return allocatedCWUt;
    }

    @Override
    public int getMaxCWUt(@NotNull Collection<IOpticalComputationProvider> seen) {
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
    public boolean canBridge(@NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);

        for (var provider : providers) {
            if (provider.canBridge(seen)) {
                return true;
            }
        }
        return false;
    }

    public static WirelessComputationStore getStore(UUID uuid) {
        if (GlobalWirelessComputation.get(uuid) == null) {
            GlobalWirelessComputation.put(uuid, new WirelessComputationStore());
        }
        return GlobalWirelessComputation.get(uuid);
    }

    public static void addHatches(UUID uuid, List<IOpticalComputationHatch> hatches) {
        var store = getStore(uuid);
        store.addProviders(hatches);
    }

    public static void removeHatches(UUID uuid, List<IOpticalComputationHatch> hatches) {
        var store = getStore(uuid);
        store.removeProviders(hatches);
    }
}
