package com.ghostipedia.cosmiccore.api.capability;

import com.ghostipedia.cosmiccore.api.noctyx.NoctyxStack;
import com.ghostipedia.cosmiccore.api.noctyx.NoctyxType;

public interface INoctyxHandler {

    NoctyxType getNoctyxType(int slot);

    NoctyxStack getNoctyxInContainer(int slot);

    void setNoctyxInContainer(int slot, NoctyxStack stack);

    default boolean supportsFill(int slot) {
        return true;
    }

    default boolean supportsDrain(int slot) {
        return true;
    }
}
