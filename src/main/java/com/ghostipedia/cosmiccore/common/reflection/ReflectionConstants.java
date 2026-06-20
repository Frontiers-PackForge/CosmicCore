package com.ghostipedia.cosmiccore.common.reflection;

/**
 * Constants for the Reflection system - erosion thresholds, costs, etc.
 */
public final class ReflectionConstants {

    private ReflectionConstants() {}

    // ---- Erosion Thresholds ----
    // These trigger mandatory reflection encounters

    public static final int[] THRESHOLDS = {
            25,   // Threshold 0: Curious - "You're starting to show wear."
            50,   // Threshold 1: Observational - "We've died enough to notice now."
            100,  // Threshold 2: Familiar - "This is becoming routine, isn't it?"
            200,  // Threshold 3: Ambiguous - "You're changing faster than I expected."
            350,  // Threshold 4: Philosophical - "Do you remember what we looked like before?"
            500,  // Threshold 5: Heavy - "Halfway to... something."
            666,  // Threshold 6: Ominous - Silence. Just stares.
            800,  // Threshold 7: Unsettling - "I'm having trouble telling us apart."
            900,  // Threshold 8: Quiet - "Almost there. Thank you for looking."
            1000  // Threshold 9: Unknown - "We're just... this now."
    };

    /**
     * Get the threshold index for a given erosion value.
     * Returns -1 if below first threshold.
     */
    public static int getThresholdIndex(int erosion) {
        for (int i = THRESHOLDS.length - 1; i >= 0; i--) {
            if (erosion >= THRESHOLDS[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Check if a new threshold was crossed.
     */
    public static boolean crossedNewThreshold(int oldErosion, int newErosion) {
        return getThresholdIndex(newErosion) > getThresholdIndex(oldErosion);
    }

    // ---- Bargain Cost Scaling ----
    // Bargains cost more at higher corruption

    public static final int[][] BARGAIN_COST_RANGES = {
            { 0, 100, 25, 50 },      // 0-100 erosion: costs 25-50
            { 101, 300, 75, 150 },   // 101-300 erosion: costs 75-150
            { 301, 500, 150, 250 },  // 301-500 erosion: costs 150-250
            { 501, 750, 250, 400 },  // 501-750 erosion: costs 250-400
            { 751, Integer.MAX_VALUE, 400, 600 } // 751+: costs 400-600
    };

    /**
     * Get the base cost range for a bargain at the given erosion level.
     * Returns [minCost, maxCost]
     */
    public static int[] getBargainCostRange(int currentErosion) {
        for (int[] range : BARGAIN_COST_RANGES) {
            if (currentErosion >= range[0] && currentErosion <= range[1]) {
                return new int[] { range[2], range[3] };
            }
        }
        // Fallback to highest tier
        return new int[] { 400, 600 };
    }

    // ---- Command Cost Escalation ----
    // /home and /back costs double with repeated use

    public static final int HOME_BASE_COST = 1;
    public static final int BACK_BASE_COST = 2;
    public static final int HOME_COST_CEILING = 16;
    public static final int BACK_COST_CEILING = 32;
    public static final int HOME_UNLOCK_COST = 12;
    public static final int BACK_UNLOCK_COST = 12;

    /** Cooldown before command usage count resets (15 minutes in milliseconds) */
    public static final long COMMAND_USAGE_RESET_TIME = 15 * 60 * 1000L;

    /**
     * Calculate command cost based on usage count.
     * Cost doubles each use until ceiling.
     */
    public static int calculateCommandCost(int baseCost, int ceiling, int usageCount) {
        if (usageCount <= 0) return baseCost;
        int cost = baseCost * (1 << usageCount); // baseCost * 2^usageCount
        return Math.min(cost, ceiling);
    }

    /**
     * Get the current cost for a command bargain, accounting for usage escalation.
     */
    public static int getCommandCost(IReflection reflection, String command) {
        int usageCount = reflection.getCommandUsageCount(command);

        return switch (command) {
            case "home" -> calculateCommandCost(HOME_BASE_COST, HOME_COST_CEILING, usageCount);
            case "back" -> calculateCommandCost(BACK_BASE_COST, BACK_COST_CEILING, usageCount);
            default -> 1;
        };
    }

    // ---- Soul Color Ranges ----
    // For visualization

    public static final int[][] SOUL_COLOR_RANGES = {
            { 0, 50 },      // Pale white/silver
            { 51, 150 },    // Faint blue tint
            { 151, 300 },   // Deep blue/purple
            { 301, 500 },   // Violet/crimson threads
            { 501, 750 },   // Dark red/black veins
            { 751, 1000 },  // Mostly black, faint glow
            { 1001, Integer.MAX_VALUE } // Void-like, inverted
    };

    /**
     * Get the soul color tier (0-6) for visualization.
     */
    public static int getSoulColorTier(int erosion) {
        for (int i = 0; i < SOUL_COLOR_RANGES.length; i++) {
            if (erosion >= SOUL_COLOR_RANGES[i][0] && erosion <= SOUL_COLOR_RANGES[i][1]) {
                return i;
            }
        }
        return SOUL_COLOR_RANGES.length - 1;
    }

    // ---- Defiance Costs ----

    /** Multiplier for defiance erosion spike (applied to original bargain cost) */
    public static final float DEFIANCE_COST_MULTIPLIER = 2.5f;

    // ---- Awakening ----

    /** Number of deaths before the reflection awakens */
    public static final int DEATHS_TO_AWAKEN = 3;

    // ---- Blink Power ----

    /** Erosion cost per blink use */
    public static final float BLINK_EROSION_PER_USE = 0.5f;
}
