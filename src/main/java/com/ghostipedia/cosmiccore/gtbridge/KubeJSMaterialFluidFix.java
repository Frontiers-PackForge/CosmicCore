package com.ghostipedia.cosmiccore.gtbridge;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class KubeJSMaterialFluidFix {

    private KubeJSMaterialFluidFix() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerMissedMaterialFluids(RegisterEvent event) {
        if (event.getRegistryKey() != GTRegistries.MATERIAL_REGISTRY) {
            return;
        }
        for (Material material : GTRegistries.MATERIALS) {
            FluidProperty fluidProperty = material.getProperty(PropertyKey.FLUID);
            if (fluidProperty == null) {
                continue;
            }
            boolean alreadyRegistered = fluidProperty.getEntry(FluidStorageKeys.LIQUID) != null ||
                    fluidProperty.getEntry(FluidStorageKeys.GAS) != null ||
                    fluidProperty.getEntry(FluidStorageKeys.PLASMA) != null ||
                    fluidProperty.getEntry(FluidStorageKeys.MOLTEN) != null;
            if (alreadyRegistered) {
                continue;
            }
            fluidProperty.registerFluids(material, GTRegistrate.createIgnoringListenerErrors(material.getModid()));
        }
    }
}
