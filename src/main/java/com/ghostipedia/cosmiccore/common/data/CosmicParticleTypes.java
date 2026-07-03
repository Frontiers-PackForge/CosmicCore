package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class CosmicParticleTypes {

    private CosmicParticleTypes() {}

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister
            .create(Registries.PARTICLE_TYPE, CosmicCore.MOD_ID);

    public static final Supplier<SimpleParticleType> MURK = PARTICLE_TYPES.register("murk",
            () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> MURK_PALE = PARTICLE_TYPES.register("murk_pale",
            () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> MURK_MOTE = PARTICLE_TYPES.register("murk_mote",
            () -> new SimpleParticleType(false));
}
