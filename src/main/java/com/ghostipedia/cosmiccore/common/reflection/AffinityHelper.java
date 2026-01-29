package com.ghostipedia.cosmiccore.common.reflection;

import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.BargainCategory;
import com.ghostipedia.cosmiccore.common.reflection.soul.SoulShape;

import net.minecraft.world.entity.player.Player;

/**
 * Helper for applying Soul Shape affinity multipliers to bargain effects.
 */
public final class AffinityHelper {

    private AffinityHelper() {}

    /**
     * Get the affinity multiplier for a player's bargain effect.
     *
     * @param player  the player
     * @param bargain the bargain being used
     * @return 1.5 if empowered, 0.5 if cursed, 1.0 if neutral
     */
    public static float getMultiplier(Player player, Bargain bargain) {
        return getMultiplier(player, bargain.getCategory());
    }

    /**
     * Get the affinity multiplier for a player and category.
     *
     * @param player   the player
     * @param category the bargain category
     * @return 1.5 if empowered, 0.5 if cursed, 1.0 if neutral
     */
    public static float getMultiplier(Player player, BargainCategory category) {
        return ReflectionCapability.get(player)
                .map(reflection -> reflection.getSoulShape().getAffinityMultiplier(category))
                .orElse(1.0f);
    }

    /**
     * Check if a player's soul shape empowers a category.
     */
    public static boolean isEmpowered(Player player, BargainCategory category) {
        return ReflectionCapability.get(player)
                .map(reflection -> reflection.getSoulShape().empowers(category))
                .orElse(false);
    }

    /**
     * Check if a player's soul shape curses a category.
     */
    public static boolean isCursed(Player player, BargainCategory category) {
        return ReflectionCapability.get(player)
                .map(reflection -> reflection.getSoulShape().curses(category))
                .orElse(false);
    }

    /**
     * Get the player's soul shape.
     */
    public static SoulShape getSoulShape(Player player) {
        return ReflectionCapability.get(player)
                .map(IReflection::getSoulShape)
                .orElse(SoulShape.UNSHAPED);
    }

    /**
     * Apply affinity multiplier to a value.
     *
     * @param value      base value
     * @param multiplier the affinity multiplier
     * @return modified value
     */
    public static float apply(float value, float multiplier) {
        return value * multiplier;
    }

    /**
     * Apply affinity multiplier to an int value (rounded).
     */
    public static int apply(int value, float multiplier) {
        return Math.round(value * multiplier);
    }
}
