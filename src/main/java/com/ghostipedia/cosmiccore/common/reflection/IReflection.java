package com.ghostipedia.cosmiccore.common.reflection;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Set;

/**
 * Interface for the Reflection capability - tracks player soul erosion and bargains.
 * The Reflection is YOU - a fragment of self that remembers what you were before immortality.
 */
public interface IReflection {

    // ---- Shards (Currency) ----

    /**
     * @return current shard balance (Shards of Perpetuity consumed via right-click)
     */
    int getShardBalance();

    /**
     * Sets shard balance.
     */
    void setShardBalance(int shards);

    /**
     * Adds shards to balance.
     */
    void addShards(int amount);

    /**
     * Attempts to spend shards. Returns true if successful.
     */
    boolean spendShards(int amount);

    // ---- Soul Capacity ----

    /**
     * @return base soul capacity (default 100, can be expanded)
     */
    int getBaseCapacity();

    /**
     * Sets base capacity (for permanent expansions).
     */
    void setBaseCapacity(int capacity);

    /**
     * @return bonus capacity from gear/temporary effects
     */
    int getBonusCapacity();

    /**
     * Sets bonus capacity (from curios, armor, etc.).
     */
    void setBonusCapacity(int bonus);

    /**
     * @return total capacity (base + bonus)
     */
    default int getTotalCapacity() {
        return getBaseCapacity() + getBonusCapacity();
    }

    /**
     * @return current weight used by active bargains
     */
    int getUsedCapacity();

    /**
     * @return remaining capacity available
     */
    default int getRemainingCapacity() {
        return getTotalCapacity() - getUsedCapacity();
    }

    /**
     * Check if a bargain can fit within current capacity.
     */
    default boolean canFitBargain(int weight) {
        return getRemainingCapacity() >= weight;
    }

    // ---- Erosion ----

    /**
     * @return total erosion accumulated (1 death = 1 erosion, bargains add more)
     */
    int getErosion();

    /**
     * Sets total erosion value.
     */
    void setErosion(int erosion);

    /**
     * Adds erosion. Use for deaths, bargains, power usage.
     */
    void addErosion(int amount);

    /**
     * @return total number of deaths tracked
     */
    int getDeathCount();

    /**
     * Increment death counter and add 1 erosion.
     */
    void recordDeath();

    // ---- Bargains ----

    /**
     * @return set of bargain IDs the player has accepted
     */
    Set<ResourceLocation> getActiveBargains();

    /**
     * @return true if player has this bargain active
     */
    boolean hasBargain(ResourceLocation bargainId);

    /**
     * Accept a bargain. Does NOT add erosion - caller should handle cost.
     */
    void acceptBargain(ResourceLocation bargainId);

    /**
     * Defy (remove) a bargain. The scar remains tracked separately.
     */
    void defy(ResourceLocation bargainId);

    /**
     * @return set of bargain IDs that have been defied (scars)
     */
    Set<ResourceLocation> getDefianceScars();

    /**
     * @return true if this bargain was defied (scarred)
     */
    boolean hasDefianceScar(ResourceLocation bargainId);

    // ---- Threshold Tracking ----

    /**
     * @return the highest threshold index the player has seen (0-9)
     */
    int getHighestThresholdSeen();

    /**
     * Mark a threshold as seen.
     */
    void setHighestThresholdSeen(int threshold);

    // ---- First Encounter ----

    /**
     * @return true if the reflection has awakened (first deaths occurred)
     */
    boolean hasAwakened();

    /**
     * Mark the reflection as awakened.
     */
    void setAwakened(boolean awakened);

    /**
     * @return true if the awakening sequence (first bargain offer) has been shown
     */
    boolean hasCompletedAwakeningSequence();

    /**
     * Mark the awakening sequence as completed.
     */
    void setAwakeningSequenceCompleted(boolean completed);

    // ---- Command Usage Tracking ----

    /**
     * Get recent usage count for a command (for cost escalation).
     * 
     * @param commandId the command identifier (e.g., "home", "back")
     * @return number of uses in the current escalation window
     */
    int getCommandUsageCount(String commandId);

    /**
     * Record a command use.
     */
    void recordCommandUse(String commandId);

    /**
     * Reset command usage (called when cooldown expires).
     */
    void resetCommandUsage(String commandId);

    /**
     * @return timestamp of last command use for cooldown tracking
     */
    long getLastCommandUseTime(String commandId);

    // ---- Memory / Context ----

    /**
     * Store arbitrary data for whisper/dialogue context.
     * Examples: death causes, dimensions visited, etc.
     */
    Map<String, Integer> getMemory();

    /**
     * Increment a memory counter.
     */
    void rememberEvent(String eventKey);

    /**
     * Get count for a specific memory.
     */
    int getMemoryCount(String eventKey);
}
