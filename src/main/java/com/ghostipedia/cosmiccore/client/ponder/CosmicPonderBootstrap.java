package com.ghostipedia.cosmiccore.client.ponder;

import net.createmod.ponder.foundation.PonderIndex;

public final class CosmicPonderBootstrap {

    private CosmicPonderBootstrap() {}

    public static void init() {
        PonderIndex.addPlugin(new CosmicPonderPlugin());
    }
}
