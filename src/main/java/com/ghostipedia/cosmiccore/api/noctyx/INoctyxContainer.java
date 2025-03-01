package com.ghostipedia.cosmiccore.api.noctyx;

import org.jetbrains.annotations.NotNull;

public interface INoctyxContainer {

    @NotNull
    NoctyxStack getNoctyx();

    int getNoctyxAmount();

    int getCapacity();

    boolean isNoctyxValid(int slot, @NotNull NoctyxStack stack);

    int fill(@NotNull NoctyxStack resource, boolean simulate);

    @NotNull
    NoctyxStack drain(int maxDrain, boolean simulate);

    @NotNull
    NoctyxStack drain(@NotNull NoctyxStack resource, boolean simulate);
}
