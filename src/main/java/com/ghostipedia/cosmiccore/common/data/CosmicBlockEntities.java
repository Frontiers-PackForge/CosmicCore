package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.common.blockentity.ReforgingTableTile;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;

public class CosmicBlockEntities {
    public static final BlockEntityEntry<ReforgingTableTile> REFORGING_TABLE = REGISTRATE.blockEntity("good_reforging_table",ReforgingTableTile::new)
            .validBlock(CosmicBlocks.GOOD_REFORGING_TABLE)
            .register();
    public static void init() {

    }
}
