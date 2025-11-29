package com.ghostipedia.cosmiccore.api.capability;

import com.ghostipedia.cosmiccore.api.data.souls.SoulNetwork;

import java.util.UUID;

public interface ISoulContainer {
     /**
     * @return the soul network attached to the container
     */
    SoulNetwork getSoulNetwork();
}
