package com.ghostipedia.cosmiccore.client.mirror;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public final class MirrorSounds {

    private MirrorSounds() {}

    private static void ui(SoundEvent sound, float pitch, float volume) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
    }

    public static void open() {
        ui(SoundEvents.AMETHYST_BLOCK_CHIME, 0.7f, 0.5f);
    }

    public static void hover(int index) {
        ui(SoundEvents.AMETHYST_BLOCK_CHIME, 1.5f + (index % 5) * 0.06f, 0.22f);
    }

    public static void coil() {
        ui(SoundEvents.WOOL_PLACE, 1.0f, 0.4f);
    }

    public static void weaveStart() {
        ui(SoundEvents.ENCHANTMENT_TABLE_USE, 0.9f, 0.5f);
    }

    public static void weaveComplete() {
        ui(SoundEvents.AMETHYST_BLOCK_RESONATE, 1.0f, 0.7f);
    }

    public static void weavePhase(DeedCinematic.Phase phase) {
        switch (phase) {
            case PRELUDE -> ui(SoundEvents.SCULK_CLICKING, 0.6f, 0.25f);
            case COIL -> ui(SoundEvents.WOOL_PLACE, 0.75f, 0.55f);
            case RING -> ui(SoundEvents.AMETHYST_BLOCK_RESONATE, 0.8f, 0.65f);
            case KNOT -> ui(SoundEvents.RESPAWN_ANCHOR_CHARGE, 1.15f, 0.55f);
        }
    }

    public static void memoryGlyph(DeedCinematic.Phase phase) {
        float pitch = 0.68f + phase.ordinal() * 0.11f;
        ui(SoundEvents.SCULK_CLICKING, pitch, 0.12f);
    }

    public static void holdTick(float progress) {
        ui(SoundEvents.AMETHYST_BLOCK_STEP, 0.8f + progress * 0.8f, 0.4f);
    }

    public static void claim() {
        ui(SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, 1.3f, 0.45f);
        ui(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.2f, 0.3f);
    }

    public static void compress() {
        ui(SoundEvents.CONDUIT_ACTIVATE, 0.8f, 0.55f);
        ui(SoundEvents.AMETHYST_BLOCK_RESONATE, 0.6f, 0.5f);
    }

    public static void heartStage(int stage) {
        ui(SoundEvents.AMETHYST_BLOCK_RESONATE, 0.7f + stage * 0.25f, 0.8f);
        ui(SoundEvents.RESPAWN_ANCHOR_CHARGE, 1.0f + stage * 0.2f, 0.5f);
    }

    public static void heartClaim() {
        ui(SoundEvents.AMETHYST_BLOCK_RESONATE, 1.4f, 1.0f);
        ui(SoundEvents.CONDUIT_ACTIVATE, 0.6f, 0.8f);
        ui(SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, 0.8f, 0.7f);
        ui(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.9f, 0.4f);
    }
}
