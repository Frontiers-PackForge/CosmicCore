package com.ghostipedia.cosmiccore.common.airControl;

import net.minecraft.world.item.ItemStack;

public interface IOxygenSupplyItem {

    /**
     * Try to provide up to requestTicks of oxygen from this stack.
     * 
     * @return ticks actually provided (0..requestTicks)
     */
    int drainOxygenTicks(ItemStack stack, int requestTicks);
}
