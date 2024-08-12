package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.common.blockentity.CosmicCoilBlockEntity;
import com.gregtechceu.gtceu.common.blockentity.CableBlockEntity;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.lowdragmc.lowdraglib.client.renderer.ATESRRendererProvider;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class CosmicBlockEntities {
    public static final BlockEntityEntry<CosmicCoilBlockEntity> CAUSAL_FABRIC_COIL_BLOCK_ENTITY = REGISTRATE
            .blockEntity("causal_fabric_coil", CosmicCoilBlockEntity::new)
            .renderer(() -> ctx -> new ATESRRendererProvider<>())
            .validBlocks(CosmicBlocks.COIL_CAUSAL_FABRIC)
            .register();


    public static void init() {

    }
}
