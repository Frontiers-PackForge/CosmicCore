package com.ghostipedia.cosmiccore.common.mob;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines mob stat scaling per dimension.
 * Higher tier dimensions have stronger mobs to match progression.
 */
public final class DimensionMobScaling {

    private DimensionMobScaling() {}

    /**
     * Scaling configuration for a dimension.
     */
    public static class ScalingConfig {

        public final double healthMultiplier;
        public final double damageMultiplier;
        public final double armorMultiplier;
        public final double speedMultiplier;

        public ScalingConfig(double health, double damage, double armor, double speed) {
            this.healthMultiplier = health;
            this.damageMultiplier = damage;
            this.armorMultiplier = armor;
            this.speedMultiplier = speed;
        }

        /**
         * Create a config with just health/damage multipliers (armor and speed unchanged).
         */
        public static ScalingConfig basic(double health, double damage) {
            return new ScalingConfig(health, damage, 1.0, 1.0);
        }

        /**
         * Create a config with all stats scaled uniformly.
         */
        public static ScalingConfig uniform(double multiplier) {
            return new ScalingConfig(multiplier, multiplier, multiplier, 1.0);
        }
    }

    // Default config for unregistered dimensions (no scaling)
    public static final ScalingConfig DEFAULT = new ScalingConfig(1.0, 1.0, 1.0, 1.0);

    // Dimension -> Scaling config map
    private static final Map<ResourceKey<Level>, ScalingConfig> DIMENSION_SCALING = new HashMap<>();

    // --- Dimension Keys ---

    // Vanilla
    public static final ResourceKey<Level> NETHER = Level.NETHER;
    public static final ResourceKey<Level> END = Level.END;

    // Aether
    private static ResourceKey<Level> aetherDim(String name) {
        return ResourceKey.create(Registries.DIMENSION, new ResourceLocation("aether", name));
    }

    public static final ResourceKey<Level> AETHER = aetherDim("the_aether");

    // Undergarden
    private static ResourceKey<Level> undergardenDim(String name) {
        return ResourceKey.create(Registries.DIMENSION, new ResourceLocation("undergarden", name));
    }

    public static final ResourceKey<Level> UNDERGARDEN = undergardenDim("undergarden");

    // Ad Astra
    private static ResourceKey<Level> adAstraDim(String name) {
        return ResourceKey.create(Registries.DIMENSION, new ResourceLocation("ad_astra", name));
    }

    public static final ResourceKey<Level> MOON = adAstraDim("moon");
    public static final ResourceKey<Level> MOON_ORBIT = adAstraDim("moon_orbit");
    public static final ResourceKey<Level> MARS = adAstraDim("mars");
    public static final ResourceKey<Level> MARS_ORBIT = adAstraDim("mars_orbit");
    public static final ResourceKey<Level> VENUS = adAstraDim("venus");
    public static final ResourceKey<Level> VENUS_ORBIT = adAstraDim("venus_orbit");
    public static final ResourceKey<Level> MERCURY = adAstraDim("mercury");
    public static final ResourceKey<Level> MERCURY_ORBIT = adAstraDim("mercury_orbit");
    public static final ResourceKey<Level> GLACIO = adAstraDim("glacio");
    public static final ResourceKey<Level> GLACIO_ORBIT = adAstraDim("glacio_orbit");
    public static final ResourceKey<Level> EARTH_ORBIT = adAstraDim("earth_orbit");

    // Frontiers - The Deep Below
    private static ResourceKey<Level> frontiersDim(String name) {
        return ResourceKey.create(Registries.DIMENSION, new ResourceLocation("frontiers", name));
    }

    public static final ResourceKey<Level> DEEP_BELOW = frontiersDim("the_deep_below");

    /**
     * Register all dimension scaling configurations.
     * Called during mod initialization.
     */
    public static void registerScaling() {
        // Tier 1: Nether - 2x HP, 1.5x damage
        register(NETHER, ScalingConfig.basic(2.0, 1.5));

        // Tier 2: Aether - 3x HP, 2x damage
        register(AETHER, ScalingConfig.basic(3.0, 2.0));

        // Tier 3: Undergarden - 4x HP, 2.5x damage
        register(UNDERGARDEN, ScalingConfig.basic(4.0, 2.5));

        // Tier 4: Ad Astra planets/orbits - 5x HP, 3x damage
        for (ResourceKey<Level> dim : new ResourceKey[] {
                MOON, MOON_ORBIT, MARS, MARS_ORBIT, VENUS, VENUS_ORBIT,
                MERCURY, MERCURY_ORBIT, GLACIO, GLACIO_ORBIT, EARTH_ORBIT
        }) {
            register(dim, ScalingConfig.basic(5.0, 3.0));
        }

        // Tier 5: The Deep Below - 6x HP, 4x damage
        register(DEEP_BELOW, ScalingConfig.basic(6.0, 4.0));

        // The End - 2.5x HP, 2x damage (between Nether and Aether)
        register(END, ScalingConfig.basic(2.5, 2.0));
    }

    /**
     * Register scaling for a dimension.
     */
    public static void register(ResourceKey<Level> dimension, ScalingConfig config) {
        DIMENSION_SCALING.put(dimension, config);
    }

    /**
     * Get the scaling config for a dimension.
     * Returns DEFAULT if dimension has no registered scaling.
     */
    public static ScalingConfig getScaling(ResourceKey<Level> dimension) {
        return DIMENSION_SCALING.getOrDefault(dimension, DEFAULT);
    }

    /**
     * Check if a dimension has custom scaling registered.
     */
    public static boolean hasScaling(ResourceKey<Level> dimension) {
        return DIMENSION_SCALING.containsKey(dimension);
    }
}
