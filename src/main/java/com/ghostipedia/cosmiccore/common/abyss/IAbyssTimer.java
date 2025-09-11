package com.ghostipedia.cosmiccore.common.abyss;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;


public interface IAbyssTimer {

    long getRemainingTicks(ResourceKey<Level> dimension);

    void setRemainingTicks(ResourceKey<Level> dimension, long ticks);

    boolean isDecaying(ResourceKey<Level> dimension);

    void setDecaying(ResourceKey<Level> dimension, boolean decaying);

    double getCleanse(ResourceKey<Level> dimension);

    void setCleanse(ResourceKey<Level> dimension, double amount);




}
