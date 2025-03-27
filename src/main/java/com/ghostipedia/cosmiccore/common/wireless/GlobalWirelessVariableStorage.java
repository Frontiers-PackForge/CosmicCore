package com.ghostipedia.cosmiccore.common.wireless;

import java.util.HashMap;
import java.util.UUID;

public abstract class GlobalWirelessVariableStorage {

    // Global wireless data stick map
    public static HashMap<UUID, WirelessDataStore> GlobalWirelessDataSticks = new HashMap<>(100, 0.9f);
}
