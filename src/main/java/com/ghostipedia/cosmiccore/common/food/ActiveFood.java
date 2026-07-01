package com.ghostipedia.cosmiccore.common.food;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ActiveFood {

    public final Item item;
    public final FoodDefinition def;
    public int ticksLeft;

    public ActiveFood(Item item, int ticksLeft) {
        this.item = item;
        this.ticksLeft = ticksLeft;
        this.def = CosmicFoodRegistry.get(new ItemStack(item));
    }
}
