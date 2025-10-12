package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.ember.blockentity.CosmicEmberEmitterBlockEntity;
import com.ghostipedia.cosmiccore.ember.blockentity.CosmicEmberReceptorBlockEntity;

import com.gregtechceu.gtceu.api.GTValues;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;

import java.util.Locale;
import java.util.Map;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.EMBER_EMITTER_BLOCKS;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.EMBER_RECEPTOR_BLOCKS;

public class CosmicBlockEntities {

    static {
        CosmicBlocks.COSMIC_EMBER_EMITTER_STEAM.getId();
        EMBER_EMITTER_BLOCKS.size();
        EMBER_RECEPTOR_BLOCKS.size();
    }

    public static final Map<Integer, BlockEntityEntry<CosmicEmberEmitterBlockEntity>> COSMIC_EMBER_EMITTER_BE = registerEmberEmitters();
    public static final Map<Integer, BlockEntityEntry<CosmicEmberReceptorBlockEntity>> COSMIC_EMBER_RECEIVER_BE = registerEmberReceptors();

    private static Map<Integer, BlockEntityEntry<CosmicEmberEmitterBlockEntity>> registerEmberEmitters() {
        var emitters = new it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<BlockEntityEntry<CosmicEmberEmitterBlockEntity>>();
        for (int i = 0; i < 15; i++) {
            final int tier = i;
            final String key = (i == 0 ? "steam" : GTValues.VN[i].toLowerCase(java.util.Locale.ROOT));

            var validBlock = EMBER_EMITTER_BLOCKS.get(i);
            if (validBlock == null) throw new IllegalStateException("Missing emitter block entry for tier " + i + " (" + key + ")");

            var be = REGISTRATE.<CosmicEmberEmitterBlockEntity>blockEntity(
                            "cosmic_%s_ember_emitter_be".formatted(key),
                            (type, pos, state) -> new CosmicEmberEmitterBlockEntity(type, pos, state, tier)
                    )
                    .validBlock(validBlock)   // safe now
                    .register();

            emitters.put(i, be);
        }
        return emitters;
    }

    private static Map<Integer, BlockEntityEntry<CosmicEmberReceptorBlockEntity>> registerEmberReceptors() {
        var receptors = new Int2ObjectOpenHashMap<BlockEntityEntry<CosmicEmberReceptorBlockEntity>>();
        for (int i = 0; i < 15; i++) {
            final int tier = i;
            final String key = (i == 0 ? "steam" : GTValues.VN[i].toLowerCase(java.util.Locale.ROOT));

            var validBlock = EMBER_RECEPTOR_BLOCKS.get(i);
            if (validBlock == null) throw new IllegalStateException("Missing receptor block entry for tier " + i + " (" + key + ")");

            var be = REGISTRATE.<CosmicEmberReceptorBlockEntity>blockEntity(
                            "cosmic_%s_ember_receiver_be".formatted(key),
                            (type, pos, state) -> new CosmicEmberReceptorBlockEntity(type, pos, state, tier)
                    )
                    .validBlock(validBlock)
                    .register();

            receptors.put(i, be);
        }
        return receptors;
    }

    public static void init() {}
}

