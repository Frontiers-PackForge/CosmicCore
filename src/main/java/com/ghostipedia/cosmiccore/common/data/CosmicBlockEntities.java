package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.client.renderer.block.NebulaeCoilRenderer;
import com.ghostipedia.cosmiccore.client.renderer.blockentity.NoctyxRelayRenderer;
import com.ghostipedia.cosmiccore.common.blockentity.CosmicCoilBlockEntity;
import com.ghostipedia.cosmiccore.common.blockentity.NoctyxBlockEntity;

import com.tterrag.registrate.util.entry.BlockEntityEntry;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

@SuppressWarnings("Convert2MethodRef")
public class CosmicBlockEntities {

    public static final BlockEntityEntry<CosmicCoilBlockEntity> CAUSAL_FABRIC_COIL_BLOCK_ENTITY = REGISTRATE
            .blockEntity("causal_fabric_coil", CosmicCoilBlockEntity::new)
            .renderer(() -> NebulaeCoilRenderer.createBlockEntityRenderer())
            .validBlocks(CosmicBlocks.COIL_CAUSAL_FABRIC)
            .register();

    public static final BlockEntityEntry<NoctyxBlockEntity> NOCTYX_BLOCK_ENTITY = REGISTRATE
            .blockEntity("noctyx_block_entity", NoctyxBlockEntity::new)
            .renderer(() -> NoctyxRelayRenderer::new)
            .validBlocks(CosmicBlocks.NOCTYX_CONNECTOR_BLOCK, CosmicBlocks.NOCTYX_RELAY_BLOCK)
            .register();

    public static void init() {}
}
