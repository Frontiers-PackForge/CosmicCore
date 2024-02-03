package com.ghostipedia.cosmiccore.gtbridge.pipenet.blockentities;


import com.ghostipedia.cosmiccore.api.registries.CosmicRegistries;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.tterrag.registrate.util.entry.BlockEntityEntry;



public class CosmicBlockEntities {

    public static final BlockEntityEntry<PressurePipeBlockEntity> PRESSURE_PIPE = CosmicRegistries.COSMIC_REGISTRATE
            .blockEntity("pressure_pipe", PressurePipeBlockEntity::new)
            .validBlocks(CosmicBlocks.PRESSURE_PIPE_BLOCKS)
            .register();

    public static void init() {

    }
}
