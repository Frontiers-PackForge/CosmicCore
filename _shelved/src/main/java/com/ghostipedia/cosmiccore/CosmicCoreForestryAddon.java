package com.ghostipedia.cosmiccore;

import net.minecraft.resources.ResourceLocation;

import forestry.api.modules.IForestryModule;
import org.jetbrains.annotations.NotNull;

public class CosmicCoreForestryAddon implements IForestryModule {

    @Override
    public @NotNull ResourceLocation getId() {
        return CosmicCore.id("core/cosmicore");
    }
}
