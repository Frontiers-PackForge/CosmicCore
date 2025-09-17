package com.ghostipedia.cosmiccore.common.airControl;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class OxygenItemCap {
    private OxygenItemCap() {}

    public static final Capability<IOxygenSupplyItem> OXYGEN_SUPPLY =
            CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent
    public static void onRegisterCaps(RegisterCapabilitiesEvent event) {
        event.register(IOxygenSupplyItem.class);
    }
}