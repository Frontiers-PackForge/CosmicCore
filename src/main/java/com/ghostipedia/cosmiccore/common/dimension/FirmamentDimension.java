package com.ghostipedia.cosmiccore.common.dimension;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class FirmamentDimension {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("frontiers", "firmament");
    public static final ResourceKey<Level> KEY = ResourceKey.create(Registries.DIMENSION, ID);

    private FirmamentDimension() {}
}
