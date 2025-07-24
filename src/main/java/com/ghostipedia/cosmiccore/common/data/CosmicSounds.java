package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.api.sound.SoundEntry;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class CosmicSounds {

    public static final SoundEntry BLACK_HOLE_CRY = REGISTRATE.sound(CosmicCore.id("ambient_drone")).build();
    public static final SoundEntry ARCANE_DISTIL = REGISTRATE.sound(CosmicCore.id("arcane_distil")).build();
    public static final SoundEntry MINING_MACHINE = REGISTRATE.sound(CosmicCore.id("mining_machine")).build();

    public static void init() {}
}
