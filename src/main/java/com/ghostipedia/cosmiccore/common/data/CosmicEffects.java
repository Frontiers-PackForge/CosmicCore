package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CosmicEffects {

    private CosmicEffects() {}

    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT,
            CosmicCore.MOD_ID);

    public static final DeferredHolder<MobEffect, StealthEffect> STEALTH = EFFECTS.register("stealth",
            StealthEffect::new);

    public static class StealthEffect extends MobEffect {

        StealthEffect() {
            super(MobEffectCategory.BENEFICIAL, 0x9CC3D6);
        }
    }
}
