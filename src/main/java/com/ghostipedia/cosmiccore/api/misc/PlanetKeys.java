package com.ghostipedia.cosmiccore.api.misc;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class PlanetKeys {

    public static final ResourceKey<Level> SUN;
    public static final ResourceKey<Level> JUPITER;
    public static final ResourceKey<Level> SATURN;

    static {
        SUN = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("frontiers", "sun"));
        JUPITER = ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath("frontiers", "jupiter"));
        SATURN = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("frontiers", "saturn"));
    }
}
