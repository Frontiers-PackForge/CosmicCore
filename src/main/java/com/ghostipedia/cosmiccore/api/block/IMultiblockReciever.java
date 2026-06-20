package com.ghostipedia.cosmiccore.api.block;

import javax.annotation.Nullable;

public interface IMultiblockReciever {

    @Nullable
    IMultiblockProvider getModularMultiBlock();

    void setModularMultiBlock(IMultiblockProvider provider);

    void sendWorkingDisabled();

    void sendWorkingEnabled();

    String getNameForDisplays();
}
