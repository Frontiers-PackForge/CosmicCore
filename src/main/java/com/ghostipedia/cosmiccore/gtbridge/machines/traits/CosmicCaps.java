package com.ghostipedia.cosmiccore.gtbridge.machines.traits;

import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class CosmicCaps {

    public static Capability<IAirHandlerMachine> CAPABILITY_PRESSURE_CONTAINER = CapabilityManager.get(new CapabilityToken<> (){});

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IAirHandlerMachine.class);
    }
}
