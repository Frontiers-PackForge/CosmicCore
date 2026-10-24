package com.ghostipedia.cosmiccore.common.armor;

import com.gregtechceu.gtceu.utils.input.SyncedKeyMapping;

import net.neoforged.neoforge.client.settings.KeyConflictContext;

import com.mojang.blaze3d.platform.InputConstants;

public final class SanguineKeyMappings {

    public static final SyncedKeyMapping ARMOR_MODE_SWITCH = SyncedKeyMapping.createConfigurable(
            "gtceu.key.armor_mode_switch", KeyConflictContext.IN_GAME, InputConstants.KEY_M);
    public static final SyncedKeyMapping BOOTS_ENABLE = SyncedKeyMapping.createConfigurable(
            "gtceu.key.enable_boots", KeyConflictContext.IN_GAME, InputConstants.KEY_PERIOD);
    public static final SyncedKeyMapping ARMOR_CHARGING = SyncedKeyMapping.createConfigurable(
            "gtceu.key.armor_charging", KeyConflictContext.IN_GAME, InputConstants.KEY_N);
    public static final SyncedKeyMapping STEP_ASSIST_ENABLE = SyncedKeyMapping.createConfigurable(
            "gtceu.key.enable_step_assist", KeyConflictContext.IN_GAME, InputConstants.KEY_APOSTROPHE);

    private SanguineKeyMappings() {}

    public static void init() {}
}
