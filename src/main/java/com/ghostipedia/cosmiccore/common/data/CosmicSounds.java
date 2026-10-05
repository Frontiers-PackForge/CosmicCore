package com.ghostipedia.cosmiccore.common.data;

import com.gregtechceu.gtceu.api.sound.SoundEntry;

import net.neoforged.neoforge.registries.DeferredHolder;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;

public class CosmicSounds {

    public static final DeferredHolder<SoundEntry, SoundEntry> BLACK_HOLE_CRY = REGISTRATE.sound("ambient_drone")
            .register();
    public static final DeferredHolder<SoundEntry, SoundEntry> STELLAR_BODY_DYING = REGISTRATE.sound("dying_star")
            .register();
    public static final DeferredHolder<SoundEntry, SoundEntry> ARCANE_DISTIL = REGISTRATE.sound("arcane_distil")
            .register();
    public static final DeferredHolder<SoundEntry, SoundEntry> MINING_MACHINE = REGISTRATE.sound("mining_machine")
            .register();
    public static final DeferredHolder<SoundEntry, SoundEntry> GAS_SUCC = REGISTRATE.sound("gas_succ").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> HEAVY_ASSEM = REGISTRATE.sound("heavy_assembler")
            .register();
    public static final DeferredHolder<SoundEntry, SoundEntry> LAMINATOR = REGISTRATE.sound("laminator").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> FLUIDIZER = REGISTRATE.sound("fluidizer").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> ORBITAL_FORGE = REGISTRATE.sound("orbital_forge")
            .register();
    public static final DeferredHolder<SoundEntry, SoundEntry> CHEMVAT = REGISTRATE.sound("icv").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> VOARX = REGISTRATE.sound("vorax").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> DAWN_FORGE_SFX = REGISTRATE.sound("dawnforge")
            .register();
    public static final DeferredHolder<SoundEntry, SoundEntry> SHAKE_CAN = REGISTRATE.sound("shake").register();

    public static void init() {}
}
