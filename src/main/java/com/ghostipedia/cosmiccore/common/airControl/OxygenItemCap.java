package com.ghostipedia.cosmiccore.common.airControl;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.neoforged.neoforge.capabilities.ItemCapability;

public class OxygenItemCap {

    private OxygenItemCap() {}

    public static final ItemCapability<IOxygenSupplyItem, Void> OXYGEN_SUPPLY = ItemCapability
            .createVoid(CosmicCore.id("oxygen_supply"), IOxygenSupplyItem.class);
}
