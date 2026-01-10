package com.ghostipedia.cosmiccore.common.airControl;

/**
 * Configuration constants for the oxygen system.
 */
public final class OxygenConfig {

    private OxygenConfig() {}

    // -------------------------------------------------------------------------
    // Oxygen Budget
    // -------------------------------------------------------------------------

    /** Maximum oxygen capacity in ticks (90 seconds) */
    public static final long MAX_OXYGEN_TICKS = 20L * 90;

    /** Seconds remaining at which to show warnings */
    public static final int[] WARNING_SECONDS = { 60, 30, 15, 10, 5 };

    // -------------------------------------------------------------------------
    // Tank Behavior
    // -------------------------------------------------------------------------

    /** Extra ticks tanks can top-up per game tick when protecting player */
    public static final int TANK_TOPUP_TICKS_PER_TICK = 2;

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
    // Rebreather Behavior
    // -------------------------------------------------------------------------

    /** Drain reduction multiplier for simple rebreather in THIN air (0.5 = half drain) */
    public static final double SIMPLE_REBREATHER_DRAIN_MULT = 0.5;

    /** Drain reduction multiplier for pressurized rebreather (0.25 = quarter drain) */
    public static final double PRESSURIZED_REBREATHER_DRAIN_MULT = 0.25;
}
