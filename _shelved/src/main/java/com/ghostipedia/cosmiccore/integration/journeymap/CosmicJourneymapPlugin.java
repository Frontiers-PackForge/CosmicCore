package com.ghostipedia.cosmiccore.integration.journeymap;

import com.ghostipedia.cosmiccore.CosmicCore;

import journeymap.client.api.ClientPlugin;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.event.RegistryEvent;
import lombok.Getter;

import java.util.EnumSet;

@ClientPlugin
public class CosmicJourneymapPlugin implements IClientPlugin {

    @Getter
    private static IClientAPI api;
    @Getter
    private static boolean active = false;
    @Getter
    private static PredictedVeinOptions options;

    @Override
    public void initialize(IClientAPI jmClientApi) {
        api = jmClientApi;
        active = true;
        jmClientApi.subscribe(CosmicCore.MOD_ID, EnumSet.of(ClientEvent.Type.REGISTRY));
        CosmicCore.LOGGER.info("CosmicCore JourneyMap integration initialized");
    }

    @Override
    public String getModId() {
        return CosmicCore.MOD_ID;
    }

    @Override
    public void onEvent(ClientEvent event) {
        if (event.type == ClientEvent.Type.REGISTRY) {
            RegistryEvent registryEvent = (RegistryEvent) event;
            if (registryEvent.getRegistryType() == RegistryEvent.RegistryType.OPTIONS) {
                options = new PredictedVeinOptions();
            }
        }
    }
}
