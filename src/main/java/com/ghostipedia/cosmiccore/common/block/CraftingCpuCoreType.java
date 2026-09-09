package com.ghostipedia.cosmiccore.common.block;

import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;

import net.minecraft.world.item.Item;

import appeng.block.crafting.ICraftingUnitType;

public enum CraftingCpuCoreType implements ICraftingUnitType {

    ACCELERATION,
    PARALLEL;

    @Override
    public long getStorageBytes() {
        return 0;
    }

    @Override
    public int getAcceleratorThreads() {
        return 0;
    }

    @Override
    public Item getItemFromType() {
        return switch (this) {
            case ACCELERATION -> CosmicBlocks.CRAFTING_ACCELERATION_CORE.asItem();
            case PARALLEL -> CosmicBlocks.CRAFTING_PARALLEL_CORE.asItem();
        };
    }
}
