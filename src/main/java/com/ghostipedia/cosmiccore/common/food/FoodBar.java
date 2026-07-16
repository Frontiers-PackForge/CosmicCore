package com.ghostipedia.cosmiccore.common.food;

import net.minecraft.world.item.ItemStack;

public record FoodBar(ItemStack icon, int ticksLeft, int base, int quality) {}
