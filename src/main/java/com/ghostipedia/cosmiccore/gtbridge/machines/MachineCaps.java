package com.ghostipedia.cosmiccore.gtbridge.machines;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class MachineCaps {

    public static Capability<IPressureContainer> CAPABILITY_PRESSURE_CONTAINER = CapabilityManager.get(new CapabilityToken<>() {});

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IPressureContainer.class);
    }
}