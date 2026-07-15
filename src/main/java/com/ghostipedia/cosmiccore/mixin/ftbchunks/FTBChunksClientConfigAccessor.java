package com.ghostipedia.cosmiccore.mixin.ftbchunks;

import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "dev.ftb.mods.ftbchunks.client.FTBChunksClientConfig", remap = false)
public interface FTBChunksClientConfigAccessor {

    @Accessor("MINIMAP_ENABLED")
    static BooleanValue cosmiccore$getMinimapEnabled() {
        throw new AssertionError();
    }
}
