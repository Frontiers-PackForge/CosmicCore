package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.client.renderer.block.NebulaeCoilRenderer;
import com.ghostipedia.cosmiccore.common.blockentity.CosmicCoilBlockEntity;

import com.tterrag.registrate.util.entry.BlockEntityEntry;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

@SuppressWarnings("Convert2MethodRef")
public class CosmicBlockEntities {

    public static final BlockEntityEntry<CosmicCoilBlockEntity> CAUSAL_FABRIC_COIL_BLOCK_ENTITY = REGISTRATE
            .blockEntity("causal_fabric_coil", CosmicCoilBlockEntity::new)
            .renderer(() -> NebulaeCoilRenderer.createBlockEntityRenderer())
            .validBlocks(CosmicBlocks.COIL_CAUSAL_FABRIC)
            .register();

    public static void init() {}
}
