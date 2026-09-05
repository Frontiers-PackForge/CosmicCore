package com.ghostipedia.cosmiccore.mixin.client.deployment;

import net.minecraft.client.particle.Particle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Particle.class)
public interface LeylineParticleAccessor {

    @Invoker("setAlpha")
    void cosmiccore$setAlpha(float alpha);
}
