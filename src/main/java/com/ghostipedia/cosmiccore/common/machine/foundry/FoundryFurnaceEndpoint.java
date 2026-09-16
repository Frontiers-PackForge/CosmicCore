package com.ghostipedia.cosmiccore.common.machine.foundry;

import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public interface FoundryFurnaceEndpoint {

    UUID foundryOwner();

    ResourceLocation foundryFurnaceType();

    FoundryRenderAnchor foundryReceivingAnchor();

    boolean isFoundryStructureFormed();

    boolean isFoundryWorking();
}
