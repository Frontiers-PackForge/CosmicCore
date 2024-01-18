package com.ghostipedia.cosmiccore.gtbridge.machines.capability;

import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

public class AirCap {

    public static final Capability<IAirHandlerMachine> AIR_HANDLER_MACHINE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IAirHandlerMachine.class);
    }
}
