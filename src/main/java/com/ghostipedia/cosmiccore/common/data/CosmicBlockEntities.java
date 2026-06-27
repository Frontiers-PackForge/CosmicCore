package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.ember.blockentity.CosmicEmberEmitterBlockEntity;
import com.ghostipedia.cosmiccore.ember.blockentity.CosmicEmberReceptorBlockEntity;

import com.gregtechceu.gtceu.api.GTValues;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.Locale;
import java.util.Map;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.EMBER_EMITTER_BLOCKS;
import static com.ghostipedia.cosmiccore.common.data.CosmicBlocks.EMBER_RECEPTOR_BLOCKS;

public class CosmicBlockEntities {

    public static final Map<Integer, BlockEntityEntry<CosmicEmberEmitterBlockEntity>> COSMIC_EMBER_EMITTER_BE = registerEmberEmitters();
    public static final Map<Integer, BlockEntityEntry<CosmicEmberReceptorBlockEntity>> COSMIC_EMBER_RECEIVER_BE = registerEmberReceptors();

    private static Map<Integer, BlockEntityEntry<CosmicEmberEmitterBlockEntity>> registerEmberEmitters() {
        Map<Integer, BlockEntityEntry<CosmicEmberEmitterBlockEntity>> emitters = new Int2ObjectOpenHashMap<>();
        for (int i = 0; i < 15; i++) {
            final int tier = i;
            final String key = (i == 0 ? "steam" : GTValues.VN[i].toLowerCase(Locale.ROOT));
            emitters.put(i, REGISTRATE.<CosmicEmberEmitterBlockEntity>blockEntity(
                    "cosmic_%s_ember_emitter_be".formatted(key),
                    (type, pos, state) -> new CosmicEmberEmitterBlockEntity(type, pos, state, tier))
                    .validBlock(EMBER_EMITTER_BLOCKS.get(i))
                    .register());
        }
        return emitters;
    }

    private static Map<Integer, BlockEntityEntry<CosmicEmberReceptorBlockEntity>> registerEmberReceptors() {
        Map<Integer, BlockEntityEntry<CosmicEmberReceptorBlockEntity>> receptors = new Int2ObjectOpenHashMap<>();
        for (int i = 0; i < 15; i++) {
            final int tier = i;
            final String key = (i == 0 ? "steam" : GTValues.VN[i].toLowerCase(Locale.ROOT));
            receptors.put(i, REGISTRATE.<CosmicEmberReceptorBlockEntity>blockEntity(
                    "cosmic_%s_ember_receiver_be".formatted(key),
                    (type, pos, state) -> new CosmicEmberReceptorBlockEntity(type, pos, state, tier))
                    .validBlock(EMBER_RECEPTOR_BLOCKS.get(i))
                    .register());
        }
        return receptors;
    }

    public static void init() {}
}
