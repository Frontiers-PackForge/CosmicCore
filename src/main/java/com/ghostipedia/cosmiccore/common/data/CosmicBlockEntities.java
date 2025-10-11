package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.ember.blockentity.CosmicEmberEmitterBlockEntity;
import com.ghostipedia.cosmiccore.ember.blockentity.CosmicEmberReceptorBlockEntity;

import com.gregtechceu.gtceu.api.GTValues;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;

import java.util.Locale;
import java.util.Map;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;

public class CosmicBlockEntities {

    public static final Map<Integer, BlockEntityEntry<CosmicEmberEmitterBlockEntity>> COSMIC_EMBER_EMITTER_BE = registerEmberEmitters();
    public static final Map<Integer, BlockEntityEntry<CosmicEmberReceptorBlockEntity>> COSMIC_EMBER_RECEIVER_BE = registerEmberReceptors();

    public static Map<Integer, BlockEntityEntry<CosmicEmberEmitterBlockEntity>> registerEmberEmitters() {
        Map<Integer, BlockEntityEntry<CosmicEmberEmitterBlockEntity>> emitters = new Int2ReferenceArrayMap<>();

        for (int i = 0; i < 15; i++) {
            final int finalI = i;
            emitters.put(i, REGISTRATE.<CosmicEmberEmitterBlockEntity>blockEntity("cosmic_%s_ember_emitter"
                    .formatted(i == 0 ? "steam" : GTValues.VN[i].toLowerCase(Locale.ROOT)),
                    (type, pos, state) -> new CosmicEmberEmitterBlockEntity(type, pos, state, finalI))
                    .register());
        }

        return emitters;
    }

    public static Map<Integer, BlockEntityEntry<CosmicEmberReceptorBlockEntity>> registerEmberReceptors() {
        Map<Integer, BlockEntityEntry<CosmicEmberReceptorBlockEntity>> receptors = new Int2ReferenceArrayMap<>();

        for (int i = 0; i < 15; i++) {
            final int finalI = i;
            receptors.put(i, REGISTRATE.<CosmicEmberReceptorBlockEntity>blockEntity("cosmic_%s_ember_receiver"
                    .formatted(i == 0 ? "steam" : GTValues.VN[i].toLowerCase(Locale.ROOT)),
                    (type, pos, state) -> new CosmicEmberReceptorBlockEntity(type, pos, state, finalI))
                    .register());
        }

        return receptors;
    }

    public static void init() {}
}
