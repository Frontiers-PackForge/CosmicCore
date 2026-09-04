package com.ghostipedia.cosmiccore.common.transmission.graph;

import java.util.Objects;
import java.util.UUID;

public record PowerTowerSpan(UUID id, UUID firstNodeId, UUID secondNodeId, int cableVoltageTier) {

    public PowerTowerSpan {
        Objects.requireNonNull(id);
        Objects.requireNonNull(firstNodeId);
        Objects.requireNonNull(secondNodeId);
        if (firstNodeId.equals(secondNodeId))
            throw new IllegalArgumentException("A span requires two distinct nodes");
        if (cableVoltageTier < 0)
            throw new IllegalArgumentException("Voltage tier cannot be negative");
    }
}
