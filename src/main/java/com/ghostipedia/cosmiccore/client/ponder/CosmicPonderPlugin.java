package com.ghostipedia.cosmiccore.client.ponder;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.ModularPowerStation;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.registration.SharedTextRegistrationHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class CosmicPonderPlugin implements PonderPlugin {

    @Override
    public String getModId() {
        return CosmicCore.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.addStoryBoard(ModularPowerStation.MODULAR_POWER_STATION.getId(),
                "modular_power_station/assembly", ModularPowerStationPonderScenes::assembly);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {}

    @Override
    public void registerSharedText(SharedTextRegistrationHelper helper) {
        for (int index = 1; index <= 5; index++) {
            String path = "modular_power_station.text_" + index;
            helper.registerSharedText(path, Component.translatable("cosmiccore.ponder.shared." + path).getString());
        }
    }
}
