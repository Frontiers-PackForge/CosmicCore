package com.ghostipedia.cosmiccore.api.misc;

import com.ghostipedia.cosmiccore.api.capability.IHeatInfoProvider;

import net.minecraft.MethodsReturnNonnullByDefault;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HeatInfoProviderList implements IHeatInfoProvider {

    private final List<? extends IHeatInfoProvider> list;

    public HeatInfoProviderList(List<? extends IHeatInfoProvider> list) {
        this.list = list;
    }

    @Override
    public HeatInfo getHeatInfo() {
        Long capacity = 0L;
        Long stored = 0L;
        boolean overload = true;
        // This was done differently in the EnergyProviderList.
        // In our case let's just reassign the longs to their contained info..?
        // IDK go ask someone more sentient!
        for (IHeatInfoProvider heatInfoProvider : list) {
            HeatInfo heatInfo = heatInfoProvider.getHeatInfo();
            capacity = heatInfo.capacity();
            stored = heatInfo.stored();
            overload = heatInfo.overload();
        }
        return new HeatInfo(capacity, stored, overload);
    }

    @Override
    public boolean supportsImpossibleHeatValues() {
        // Not 100% confident on what this is returning, assuming it's the list of all heat providers...?
        return list.size() > 1;
    }
}
