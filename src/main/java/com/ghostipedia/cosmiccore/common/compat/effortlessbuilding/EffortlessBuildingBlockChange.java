package com.ghostipedia.cosmiccore.common.compat.effortlessbuilding;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public record EffortlessBuildingBlockChange(
                                            EffortlessBuildingBlockSnapshot before,
                                            EffortlessBuildingBlockSnapshot after, ItemStack placedItem,
                                            List<ItemStack> displacedDrops) {

    public EffortlessBuildingBlockChange {
        placedItem = placedItem.copyWithCount(1);
        displacedDrops = displacedDrops.stream().map(ItemStack::copy).toList();
    }
}
