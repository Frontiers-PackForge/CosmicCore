package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.sound.SoundEntry;

import net.minecraft.core.registries.BuiltInRegistries;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class CosmicSounds {

    public static final SoundEntry BLACK_HOLE_CRY = REGISTRATE.sound(CosmicCore.id("ambient_drone")).build();
    public static final SoundEntry STELLAR_BODY_DYING = REGISTRATE.sound(CosmicCore.id("dying_star")).build();
    public static final SoundEntry ARCANE_DISTIL = REGISTRATE.sound(CosmicCore.id("arcane_distil")).build();
    public static final SoundEntry MINING_MACHINE = REGISTRATE.sound(CosmicCore.id("mining_machine")).build();
    public static final SoundEntry GAS_SUCC = REGISTRATE.sound(CosmicCore.id("gas_succ")).build();
    public static final SoundEntry HEAVY_ASSEM = REGISTRATE.sound(CosmicCore.id("heavy_assembler")).build();
    public static final SoundEntry LAMINATOR = REGISTRATE.sound(CosmicCore.id("laminator")).build();
    public static final SoundEntry FLUIDIZER = REGISTRATE.sound(CosmicCore.id("fluidizer")).build();
    public static final SoundEntry ORBITAL_FORGE = REGISTRATE.sound(CosmicCore.id("orbital_forge")).build();
    public static final SoundEntry CHEMVAT = REGISTRATE.sound(CosmicCore.id("icv")).build();
    public static final SoundEntry VOARX = REGISTRATE.sound(CosmicCore.id("vorax")).build();
    public static final SoundEntry DAWN_FORGE_SFX = REGISTRATE.sound(CosmicCore.id("dawnforge")).build();
    public static final SoundEntry SHAKE_CAN = REGISTRATE.sound(CosmicCore.id("shake")).build();

    /**
     * GTCEu registers every SoundEntry's SoundEvent in a single pass ({@code GTSoundEntries.init}) during its
     * own RegisterEvent, which fires before ours (gtceu loads before cosmiccore), so our entries are built too
     * late to be picked up. Register our own namespace's sounds here; GTCEu's pass has already run and will not
     * run again, so there is no double registration.
     */
    public static void init() {
        for (SoundEntry entry : GTRegistries.SOUNDS) {
            if (!entry.getId().getNamespace().equals(CosmicCore.MOD_ID)) continue;
            entry.prepare();
            entry.register(soundEvent ->
                    GTRegistries.register(BuiltInRegistries.SOUND_EVENT, soundEvent.getLocation(), soundEvent));
        }
    }
}
