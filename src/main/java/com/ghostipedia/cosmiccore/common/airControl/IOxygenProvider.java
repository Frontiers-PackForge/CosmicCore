package com.ghostipedia.cosmiccore.common.airControl;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Interface for items that can provide oxygen to the CosmicCore oxygen system.
 * Implemented by space suit chestplates, oxygen tanks, etc.
 */
public interface IOxygenProvider {

    /**
     * Check if this item can currently provide oxygen.
     * @param stack The item stack
     * @param player The player wearing/holding the item
     * @return true if oxygen is available
     */
    boolean hasOxygen(ItemStack stack, Player player);

    /**
     * Consume oxygen from this provider.
     * @param stack The item stack
     * @param player The player
     * @param amount Amount to consume (in millibuckets for fluid tanks, or ticks for other systems)
     * @return Amount actually consumed
     */
    long consumeOxygen(ItemStack stack, Player player, long amount);

    /**
     * Get current oxygen amount.
     * @param stack The item stack
     * @return Current oxygen in millibuckets (or equivalent units)
     */
    long getOxygenAmount(ItemStack stack);

    /**
     * Get maximum oxygen capacity.
     * @param stack The item stack
     * @return Maximum capacity in millibuckets (or equivalent units)
     */
    long getMaxOxygenCapacity(ItemStack stack);
}
