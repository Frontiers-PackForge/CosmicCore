package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.ember.CosmicEmberEmitterBlock;
import com.ghostipedia.cosmiccore.ember.blockentity.CosmicEmberEmitterBlockEntity;
import com.ghostipedia.cosmiccore.ember.blockentity.CosmicEmberReceptorBlockEntity;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.COSMIC_EMBER_EMITTER_STEAM;

public class CosmicBlockEntities {


    public static final BlockEntityEntry<CosmicEmberEmitterBlockEntity> COSMIC_EMBER_EMITTER_BE =
            REGISTRATE.<CosmicEmberEmitterBlockEntity>blockEntity("cosmic_ember_emitter_be", (type,pos,state)
                    -> new CosmicEmberEmitterBlockEntity(type,pos,state,0))
                    .validBlocks(COSMIC_EMBER_EMITTER_STEAM)
                    .register();


    public static final BlockEntityEntry<CosmicEmberReceptorBlockEntity> COSMIC_EMBER_RECEIVER_BE =
            REGISTRATE.<CosmicEmberReceptorBlockEntity>blockEntity("cosmic_ember_receiver_be", (type,pos,state)
                            -> new CosmicEmberReceptorBlockEntity(type,pos,state,0))
                    .validBlocks(
                            COSMIC_EMBER_EMITTER_STEAM
                    )
                    .register();





    public static void init() {}
}
