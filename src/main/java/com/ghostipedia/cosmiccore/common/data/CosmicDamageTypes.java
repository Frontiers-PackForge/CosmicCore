package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

public final class CosmicDamageTypes {

    private CosmicDamageTypes() {}

    public static final ResourceKey<DamageType> MURKBLOOM = ResourceKey.create(Registries.DAMAGE_TYPE,
            CosmicCore.id("murkbloom"));
    public static final ResourceKey<DamageType> TOO_LOUD = ResourceKey.create(Registries.DAMAGE_TYPE,
            CosmicCore.id("too_loud"));

    public static DamageSource source(Level level, ResourceKey<DamageType> key) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key));
    }
}
