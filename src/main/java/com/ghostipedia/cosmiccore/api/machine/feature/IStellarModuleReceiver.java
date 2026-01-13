package com.ghostipedia.cosmiccore.api.machine.feature;

import org.jetbrains.annotations.Nullable;

/**
 * Interface for Stellar Iris modules.
 * Modules receive connection from the Iris controller and processing parameters.
 */
public interface IStellarModuleReceiver {

    /**
     * @return the stellar iris this module is connected to, or null if not connected
     */
    @Nullable
    IStellarIrisProvider getStellarIris();

    /**
     * Sets the stellar iris connection for this module.
     * Called by the Iris controller when structure forms/invalidates.
     *
     * @param provider the iris provider to connect to, or null to disconnect
     */
    void setStellarIris(@Nullable IStellarIrisProvider provider);

    /**
     * @return true if this module is connected to a valid, formed Iris
     */
    default boolean isConnectedToIris() {
        IStellarIrisProvider iris = getStellarIris();
        return iris != null && iris.isFormed();
    }
}
