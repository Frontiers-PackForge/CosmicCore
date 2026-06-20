package com.ghostipedia.cosmiccore.common.wireless;

import java.util.HashMap;
import java.util.UUID;

public abstract class GlobalWirelessVariableStorage {
    // --------------------- NEVER access these maps! Use the methods provided! ---------------------

    // Global wireless data stick map
    public static HashMap<UUID, WirelessDataStore> GlobalWirelessDataSticks = new HashMap<>(20, 0.9f);
}
