package com.ghostipedia.cosmiccore.common.compat.occultism;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.klikli_dev.occultism.common.ritual.RitualFactory;
import com.klikli_dev.occultism.registry.OccultismRituals;

public final class CosmicRituals {

    private static final DeferredRegister<RitualFactory> RITUAL_FACTORIES = DeferredRegister
            .create(OccultismRituals.RITUAL_FACTORIES_KEY, CosmicCore.MOD_ID);

    public static final DeferredHolder<RitualFactory, RitualFactory> FIRMAMENT_ASCENT = RITUAL_FACTORIES
            .register("firmament_ascent", () -> new RitualFactory(FirmamentAscentRitual::new));

    private CosmicRituals() {}

    public static void register(IEventBus modBus) {
        RITUAL_FACTORIES.register(modBus);
    }
}
