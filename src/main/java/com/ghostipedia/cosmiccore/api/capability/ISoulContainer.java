package com.ghostipedia.cosmiccore.api.capability;

import wayoftime.bloodmagic.core.data.SoulNetwork;

import java.util.UUID;

public interface ISoulContainer {

    /**
     * @return the UUID of the player associated to the container's network
     */
    UUID getOwner();

    /**
     * @param playerUUID: the player to whom we attach the container
     */
    void setOwner(UUID playerUUID);

    /**
     * @return the soul network attached to the container
     */
    SoulNetwork getSoulNetwork();
}
