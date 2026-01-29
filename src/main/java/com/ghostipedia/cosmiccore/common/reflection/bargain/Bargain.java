package com.ghostipedia.cosmiccore.common.reflection.bargain;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Optional;

/**
 * Base class for all bargains in the Reflection system.
 * A bargain represents a deal with your reflection - power for erosion.
 *
 * Economy:
 * - Shards of Perpetuity: Currency to accept bargains (quest-gated)
 * - Weight: How much soul capacity this bargain consumes (0-100 base capacity)
 * - Erosion: Consequence of accepting (accumulates, drives visuals/whispers)
 */
public abstract class Bargain {

    private final ResourceLocation id;
    private final BargainTier tier;
    private final BargainCategory category;
    private final int shardCost;
    private final int weight;
    private final int erosionCost;

    protected Bargain(ResourceLocation id, BargainTier tier, BargainCategory category, int shardCost, int weight,
                      int erosionCost) {
        this.id = id;
        this.tier = tier;
        this.category = category;
        this.shardCost = shardCost;
        this.weight = weight;
        this.erosionCost = erosionCost;
    }

    /**
     * Legacy constructor for backwards compatibility during migration.
     *
     * @deprecated Use the new constructor with category, shardCost, weight, and erosionCost
     */
    @Deprecated
    protected Bargain(ResourceLocation id, BargainTier tier, int baseCost) {
        this(id, tier, BargainCategory.UTILITY, 0, 0, baseCost);
    }

    /**
     * Legacy constructor without category - defaults to UTILITY.
     *
     * @deprecated Use the new constructor with category
     */
    @Deprecated
    protected Bargain(ResourceLocation id, BargainTier tier, int shardCost, int weight, int erosionCost) {
        this(id, tier, BargainCategory.UTILITY, shardCost, weight, erosionCost);
    }

    /**
     * @return unique identifier for this bargain
     */
    public ResourceLocation getId() {
        return id;
    }

    /**
     * @return what corruption range this bargain is available in
     */
    public BargainTier getTier() {
        return tier;
    }

    /**
     * @return what category this bargain belongs to (for affinity system)
     */
    public BargainCategory getCategory() {
        return category;
    }

    /**
     * @return shard cost to accept this bargain (Shards of Perpetuity currency)
     */
    public int getShardCost() {
        return shardCost;
    }

    /**
     * @return weight against soul capacity (base 100 capacity)
     */
    public int getWeight() {
        return weight;
    }

    /**
     * @return erosion gained when accepting this bargain
     */
    public int getErosionCost() {
        return erosionCost;
    }

    /**
     * @return base erosion cost (modified by current corruption level)
     * @deprecated Use getErosionCost() instead
     */
    @Deprecated
    public int getBaseCost() {
        return erosionCost;
    }

    /**
     * @return the display name of this bargain
     */
    public abstract Component getName();

    /**
     * @return description shown in the mirror interface
     */
    public abstract Component getDescription();

    /**
     * @return the reflection's dialogue when offering this bargain
     */
    public abstract List<Component> getOfferDialogue(Player player);

    /**
     * @return the philosophical question posed by the reflection
     */
    public abstract Component getQuestion();

    /**
     * @return possible answers to the question
     */
    public abstract List<BargainAnswer> getAnswers();

    /**
     * Check if this bargain can be offered to the player.
     * Override for custom conditions beyond tier requirements.
     */
    public boolean canOffer(Player player, int currentErosion) {
        return tier.isAvailableAt(currentErosion);
    }

    /**
     * Check if context makes this bargain relevant (for curated offers).
     * Examples: hunger bargain offered when player is starving,
     * fall damage bargain offered after dying to fall damage.
     */
    public boolean isContextuallyRelevant(Player player, BargainContext context) {
        return true; // Override in subclasses
    }

    /**
     * Called when the player accepts this bargain with a specific answer.
     * Apply the power/effects here.
     */
    public abstract void onAccept(Player player, BargainAnswer answer);

    /**
     * Called when the player defies (removes) this bargain.
     * Remove the power but NOT the debuff (scar).
     */
    public abstract void onDefy(Player player);

    /**
     * Called every tick while this bargain is active.
     * Override for passive effects, erosion-per-use, etc.
     */
    public void tick(Player player) {
        // Default: no tick behavior
    }

    /**
     * @return the soul visual transformation for this bargain
     */
    public abstract BargainVisual getSoulVisual();

    /**
     * @return display name for UI purposes
     */
    public Component getDisplayName() {
        return getName();
    }

    /**
     * @return power description lines for tooltips/hub
     */
    public List<Component> getPowerDescriptions() {
        // Default: derive from description
        return List.of(Component.literal("\u00A7a" + getDescription().getString()));
    }

    /**
     * @return drawback description lines for tooltips/hub
     */
    public List<Component> getDrawbackDescriptions() {
        // Default: generic drawback showing all costs
        List<Component> drawbacks = new java.util.ArrayList<>();
        if (shardCost > 0) {
            drawbacks.add(Component.literal("\u00A7b" + shardCost + " shards"));
        }
        if (weight > 0) {
            drawbacks.add(Component.literal("\u00A7d" + weight + " weight"));
        }
        if (erosionCost > 0) {
            drawbacks.add(Component.literal("\u00A7c+" + erosionCost + " erosion"));
        }
        return drawbacks.isEmpty() ? List.of(Component.literal("\u00A7aFree")) : drawbacks;
    }

    /**
     * @return dialogue the reflection says after accepting
     */
    public List<Component> getAcceptDialogue(Player player, BargainAnswer answer) {
        return List.of(Component.literal("How does it feel?"));
    }

    /**
     * @return dialogue the reflection says if the player refuses
     */
    public List<Component> getRefuseDialogue(Player player) {
        return List.of(Component.literal("...Maybe next time."));
    }

    /**
     * Tiers determine when bargains become available.
     */
    public enum BargainTier {

        /** Available at low corruption (0-100). Early game traps. */
        EARLY(0, 100),
        /** Available at low-mid corruption (0-300). */
        EARLY_MID(0, 300),
        /** Only available at mid corruption (100-500). */
        MID(100, 500),
        /** Only available at high corruption (300-750). */
        LATE(300, 750),
        /** Only available at very high corruption (500+). The dangerous stuff. */
        EXTREME(500, Integer.MAX_VALUE),
        /** Always available, cost scales. */
        ANY(0, Integer.MAX_VALUE);

        private final int minErosion;
        private final int maxErosion;

        BargainTier(int minErosion, int maxErosion) {
            this.minErosion = minErosion;
            this.maxErosion = maxErosion;
        }

        public boolean isAvailableAt(int erosion) {
            return erosion >= minErosion && erosion <= maxErosion;
        }

        public int getMinErosion() {
            return minErosion;
        }

        public int getMaxErosion() {
            return maxErosion;
        }
    }

    /**
     * Represents an answer choice in a bargain dialogue.
     */
    public record BargainAnswer(
                                String id,
                                Component text,
                                Optional<Component> reflectionResponse,
                                boolean grantsFullPower,
                                float costModifier,
                                List<Component> powerDescription,
                                List<Component> drawbacks) {

        public BargainAnswer(String id, Component text) {
            this(id, text, Optional.empty(), true, 1.0f, List.of(), List.of());
        }

        public BargainAnswer(String id, Component text, Component response) {
            this(id, text, Optional.of(response), true, 1.0f, List.of(), List.of());
        }

        public BargainAnswer withCostModifier(float modifier) {
            return new BargainAnswer(id, text, reflectionResponse, grantsFullPower, modifier, powerDescription,
                    drawbacks);
        }

        public BargainAnswer withReducedPower() {
            return new BargainAnswer(id, text, reflectionResponse, false, costModifier, powerDescription, drawbacks);
        }

        /**
         * Add power description lines that show on hover.
         */
        public BargainAnswer withPower(Component... powers) {
            return new BargainAnswer(id, text, reflectionResponse, grantsFullPower, costModifier, List.of(powers),
                    drawbacks);
        }

        /**
         * Add drawback/curse description lines that show on hover.
         */
        public BargainAnswer withDrawbacks(Component... curses) {
            return new BargainAnswer(id, text, reflectionResponse, grantsFullPower, costModifier, powerDescription,
                    List.of(curses));
        }

        /**
         * Add both power and drawback descriptions.
         */
        public BargainAnswer withDetails(List<Component> powers, List<Component> curses) {
            return new BargainAnswer(id, text, reflectionResponse, grantsFullPower, costModifier, powers, curses);
        }
    }

    /**
     * Visual transformation data for the soul portrait.
     */
    public record BargainVisual(
                                String visualType,
                                String description) {

        public static BargainVisual of(String type, String desc) {
            return new BargainVisual(type, desc);
        }
    }

    /**
     * Context for determining if a bargain is relevant to offer.
     */
    public record BargainContext(
                                 Optional<String> lastDeathCause,
                                 Optional<ResourceLocation> currentDimension,
                                 boolean isLowHealth,
                                 boolean isHungry,
                                 boolean isSuffocating,
                                 boolean isBurning,
                                 boolean isFreezing) {

        public static BargainContext empty() {
            return new BargainContext(
                    Optional.empty(),
                    Optional.empty(),
                    false, false, false, false, false);
        }
    }
}
