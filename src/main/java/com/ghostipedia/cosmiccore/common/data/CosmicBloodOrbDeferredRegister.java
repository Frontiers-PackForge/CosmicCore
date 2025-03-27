package com.ghostipedia.cosmiccore.common.data;

import net.minecraft.resources.ResourceLocation;

import wayoftime.bloodmagic.common.item.BloodOrb;
import wayoftime.bloodmagic.common.registration.WrappedForgeDeferredRegister;
import wayoftime.bloodmagic.common.registration.impl.BloodOrbRegistryObject;
import wayoftime.bloodmagic.impl.BloodMagicAPI;

import java.util.function.Supplier;

public class CosmicBloodOrbDeferredRegister extends WrappedForgeDeferredRegister<BloodOrb> {

    public CosmicBloodOrbDeferredRegister(String modid) {
        super(modid, BloodMagicAPI.bloodOrbRegistryName());
    }

    public BloodOrbRegistryObject<BloodOrb> register(String name, ResourceLocation rl, int tier, int capacity,
                                                     int fillRate) {
        return this.register(name, () -> {
            return new BloodOrb(rl, tier, capacity, fillRate);
        });
    }

    public <ORB extends BloodOrb> BloodOrbRegistryObject<ORB> register(String name, Supplier<? extends ORB> sup) {
        return (BloodOrbRegistryObject) this.register(name, sup, BloodOrbRegistryObject::new);
    }
}
