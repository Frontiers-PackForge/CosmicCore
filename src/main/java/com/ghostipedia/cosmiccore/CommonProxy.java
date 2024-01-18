package com.ghostipedia.cosmiccore;

import com.ghostipedia.cosmiccore.gtbridge.machines.CosmicCoreMachines;
import com.ghostipedia.cosmiccore.gtbridge.machines.capability.CosmicCapabilities;

public class CommonProxy {
    public static void init() {
        CosmicCoreMachines.init();
        CosmicCapabilities.init();

    }

}