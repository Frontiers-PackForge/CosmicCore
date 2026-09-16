package com.ghostipedia.cosmiccore.client.transmission;

final class DroneMachineLocatorPolicy {

    private DroneMachineLocatorPolicy() {}

    static Route route(int chunkDistance, int viewDistance, boolean clientChunkLoaded, boolean highlightAvailable) {
        return chunkDistance < viewDistance && clientChunkLoaded && highlightAvailable ?
                Route.HIGHLIGHT : Route.TEMPORARY_WAYPOINT;
    }

    enum Route {
        HIGHLIGHT,
        TEMPORARY_WAYPOINT
    }
}
