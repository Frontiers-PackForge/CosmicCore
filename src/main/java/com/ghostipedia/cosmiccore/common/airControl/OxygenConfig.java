package com.ghostipedia.cosmiccore.common.airControl;

import net.minecraft.world.entity.player.Player;

/**
 * Configuration constants for the oxygen system.
 */
public final class OxygenConfig {

    private OxygenConfig() {}

    // -------------------------------------------------------------------------
    // Oxygen Budget
    // -------------------------------------------------------------------------

    /** Base maximum oxygen capacity in ticks (90 seconds) */
    public static final long MAX_OXYGEN_TICKS = 20L * 90;

    public static long getMaxOxygenTicks(Player player) {
        return MAX_OXYGEN_TICKS;
    }

    /** Seconds remaining at which to show warnings */
    public static final int[] WARNING_SECONDS = { 60, 30, 15, 10, 5 };

    // -------------------------------------------------------------------------
    // Tank Behavior
    // -------------------------------------------------------------------------

    /** Extra ticks tanks can top-up per game tick when protecting player */
    public static final int TANK_TOPUP_TICKS_PER_TICK = 2;

    /** Canonical oxygen consumption in NO_AIR environments, in oxygen ticks per game tick. */
    public static final double NO_AIR_DRAIN_PER_TICK = 1.0;

    /** THIN air consumes oxygen at half the canonical NO_AIR rate. */
    public static final double THIN_AIR_DRAIN_PER_TICK = NO_AIR_DRAIN_PER_TICK * 0.5;

    /** How many oxygen ticks per mB consumed from space suits (higher = suits last longer) */
    public static final int SPACE_SUIT_TICKS_PER_MB = 5;

    // -------------------------------------------------------------------------
    // HUD Sync
    // -------------------------------------------------------------------------

    /** How often to sync oxygen HUD to client (in ticks) */
    public static final int HUD_SYNC_INTERVAL = 10;

    // -------------------------------------------------------------------------
    // Damage
    // -------------------------------------------------------------------------

    /** Interval between suffocation damage ticks */
    public static final int SUFFOCATION_DAMAGE_INTERVAL = 20;

    // -------------------------------------------------------------------------
    // Air Bladder
    // -------------------------------------------------------------------------

    public static final long AIR_BLADDER_RESTORE_TICKS = 1200;  // 60 seconds of air per charge
    public static final int AIR_BLADDER_MAX_CHARGES = 3;        // 3 uses before empty
    public static final int AIR_BLADDER_COOLDOWN = 40;          // 2 second use cooldown
}
