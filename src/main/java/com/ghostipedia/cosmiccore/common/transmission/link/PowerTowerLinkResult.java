package com.ghostipedia.cosmiccore.common.transmission.link;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public record PowerTowerLinkResult(Status status, @Nullable UUID spanId) {

    public PowerTowerLinkResult {
        Objects.requireNonNull(status);
        if ((status == Status.CREATED) != (spanId != null)) {
            throw new IllegalArgumentException("Only a created span may return a span id");
        }
    }

    public enum Status {
        CREATED,
        ENDPOINT_NOT_FOUND,
        SELF_LINK,
        TOO_FAR,
        OWNER_MISMATCH,
        PATH_CHUNK_UNLOADED,
        PATH_OBSTRUCTED,
        GRAPH_INVARIANT_REJECTED
    }

    public boolean spanCreated() {
        return status == Status.CREATED;
    }
}
