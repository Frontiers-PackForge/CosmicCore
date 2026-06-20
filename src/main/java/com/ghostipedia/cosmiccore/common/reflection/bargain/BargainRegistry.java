package com.ghostipedia.cosmiccore.common.reflection.bargain;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionCapability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Registry for all bargains in the Reflection system.
 * Bargains are registered here and can be looked up by ID or filtered by context.
 */
public final class BargainRegistry {

    private BargainRegistry() {}

    private static final Map<ResourceLocation, Bargain> BARGAINS = new LinkedHashMap<>();

    /**
     * Register a bargain. Called during mod initialization.
     */
    public static void register(Bargain bargain) {
        if (BARGAINS.containsKey(bargain.getId())) {
            CosmicCore.LOGGER.warn("Duplicate bargain registration: {}", bargain.getId());
        }
        BARGAINS.put(bargain.getId(), bargain);
        CosmicCore.LOGGER.debug("Registered bargain: {}", bargain.getId());
    }

    /**
     * Get a bargain by ID.
     */
    public static Optional<Bargain> get(ResourceLocation id) {
        return Optional.ofNullable(BARGAINS.get(id));
    }

    /**
     * Get all registered bargains.
     */
    public static Collection<Bargain> getAll() {
        return Collections.unmodifiableCollection(BARGAINS.values());
    }

    /**
     * Get all bargains available to a player at their current erosion level.
     */
    public static List<Bargain> getAvailable(Player player) {
        return ReflectionCapability.get(player).map(reflection -> {
            int erosion = reflection.getErosion();
            return BARGAINS.values().stream()
                    .filter(b -> b.canOffer(player, erosion))
                    .filter(b -> !reflection.hasBargain(b.getId()))
                    .collect(Collectors.toList());
        }).orElse(Collections.emptyList());
    }

    /**
     * Get bargains that are contextually relevant to offer.
     * Used for curated offers in the mirror interface.
     */
    public static List<Bargain> getContextualOffers(Player player, Bargain.BargainContext context, int maxOffers) {
        return ReflectionCapability.get(player).map(reflection -> {
            int erosion = reflection.getErosion();

            // Get all available bargains
            List<Bargain> available = BARGAINS.values().stream()
                    .filter(b -> b.canOffer(player, erosion))
                    .filter(b -> !reflection.hasBargain(b.getId()))
                    .collect(Collectors.toList());

            // Prioritize contextually relevant ones
            List<Bargain> relevant = available.stream()
                    .filter(b -> b.isContextuallyRelevant(player, context))
                    .limit(maxOffers)
                    .collect(Collectors.toList());

            // If we don't have enough relevant ones, fill with others
            if (relevant.size() < maxOffers) {
                available.stream()
                        .filter(b -> !relevant.contains(b))
                        .limit(maxOffers - relevant.size())
                        .forEach(relevant::add);
            }

            return relevant;
        }).orElse(Collections.emptyList());
    }

    /**
     * Get all active bargains for a player.
     */
    public static List<Bargain> getActive(Player player) {
        return ReflectionCapability.get(player).map(reflection -> reflection.getActiveBargains().stream()
                .map(BargainRegistry::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList())).orElse(Collections.emptyList());
    }

    /**
     * Get all bargains the player has defied (for showing scars).
     */
    public static List<Bargain> getDefied(Player player) {
        return ReflectionCapability.get(player).map(reflection -> reflection.getDefianceScars().stream()
                .map(BargainRegistry::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList())).orElse(Collections.emptyList());
    }

    /**
     * Check if the player can defy a specific bargain.
     */
    public static boolean canDefy(Player player, ResourceLocation bargainId) {
        return ReflectionCapability.get(player)
                .map(reflection -> reflection.hasBargain(bargainId))
                .orElse(false);
    }

    /**
     * Calculate the cost of a bargain for the player.
     * Takes into account current erosion level and bargain base cost.
     */
    public static int calculateCost(Player player, Bargain bargain) {
        return ReflectionCapability.get(player).map(reflection -> {
            int erosion = reflection.getErosion();
            int baseCost = bargain.getBaseCost();

            // Scale cost based on current erosion
            float multiplier = 1.0f;
            if (erosion > 750) multiplier = 2.0f;
            else if (erosion > 500) multiplier = 1.75f;
            else if (erosion > 300) multiplier = 1.5f;
            else if (erosion > 100) multiplier = 1.25f;

            return Math.round(baseCost * multiplier);
        }).orElse(bargain.getBaseCost());
    }

    /**
     * Calculate the defiance cost for removing a bargain.
     */
    public static int calculateDefianceCost(Player player, Bargain bargain) {
        int originalCost = calculateCost(player, bargain);
        return Math.round(originalCost * 2.5f); // Defiance costs 2.5x the original
    }

    /**
     * Calculate the defiance cost without player context (client-side).
     * Uses base cost * 2.5
     */
    public static int calculateDefianceCost(Bargain bargain) {
        return Math.round(bargain.getBaseCost() * 2.5f);
    }

    /**
     * Get available bargains based on pre-synced active and scar sets (client-side).
     */
    public static List<Bargain> getAvailable(Set<ResourceLocation> activeBargains, Set<ResourceLocation> scars) {
        return BARGAINS.values().stream()
                .filter(b -> !activeBargains.contains(b.getId()))
                .filter(b -> !scars.contains(b.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Get active bargains based on pre-synced set (client-side).
     */
    public static List<Bargain> getActive(Set<ResourceLocation> activeBargains) {
        return activeBargains.stream()
                .map(BargainRegistry::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
