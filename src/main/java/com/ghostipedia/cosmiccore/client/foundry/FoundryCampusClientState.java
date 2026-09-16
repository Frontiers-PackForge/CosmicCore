package com.ghostipedia.cosmiccore.client.foundry;

import com.ghostipedia.cosmiccore.common.machine.foundry.FoundryCampusSnapshot;

import net.minecraft.core.GlobalPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class FoundryCampusClientState {

    private static final Map<GlobalPos, FoundryCampusSnapshot> CAMPUSES = new HashMap<>();

    private FoundryCampusClientState() {}

    public static void apply(FoundryCampusSnapshot snapshot) {
        CAMPUSES.put(snapshot.core(), snapshot);
    }

    public static void remove(GlobalPos core) {
        CAMPUSES.remove(core);
    }

    public static Optional<FoundryCampusSnapshot> get(GlobalPos core) {
        return Optional.ofNullable(CAMPUSES.get(core));
    }

    public static void clear() {
        CAMPUSES.clear();
    }
}
