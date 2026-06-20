package com.ghostipedia.cosmiccore.api.capability;

public interface IHeatInfoProvider {

    record HeatInfo(Long capacity, Long stored, boolean overload) {}

    // I need some way to store all dimensions temps, idk do it in the next interface.
    HeatInfo getHeatInfo();

    boolean supportsImpossibleHeatValues();
}
