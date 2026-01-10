package com.ghostipedia.cosmiccore.common.airControl;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface IOxygen {

    long getOxygenTicks(ResourceKey<Level> dimension);
    void setOxygenTicks(ResourceKey<Level> dimension, long ticks);

    boolean isConsuming(ResourceKey<Level> dimension);
    void setConsuming(ResourceKey<Level> dimension, boolean consuming);

    double getRegenBuffer(ResourceKey<Level> dimension);
    void setRegenBuffer(ResourceKey<Level> dimension, double buffer);
}
