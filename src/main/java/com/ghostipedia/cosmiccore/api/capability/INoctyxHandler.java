package com.ghostipedia.cosmiccore.api.capability;

import com.ghostipedia.cosmiccore.api.noctyx.NoctyxStack;

import org.jetbrains.annotations.NotNull;

public interface INoctyxHandler {

    int getSlots();

    @NotNull
    NoctyxStack getNoctyxInContainer(int slot);

    int getContainerCapacity(int slot);

    boolean isNoctyxValid(int slot, @NotNull NoctyxStack stack);

    int fill(@NotNull NoctyxStack resource, boolean simulate);

    @NotNull
    NoctyxStack drain(int maxDrain, boolean simulate);

    @NotNull
    NoctyxStack drain(@NotNull NoctyxStack resource, boolean simulate);

    default boolean supportsFill(int slot) {
        return true;
    }

    default boolean supportsDrain(int slot) {
        return true;
    }
}
