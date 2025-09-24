package com.ghostipedia.cosmiccore.common.item;

import net.minecraft.world.item.ItemStack;

import forestry.core.items.ItemForestry;
import forestry.core.items.definitions.IColoredItem;

public class CosmicBeesItemHoneyComb extends ItemForestry implements IColoredItem {

    private final CosmicBeesHoneyComb type;

    public CosmicBeesItemHoneyComb(CosmicBeesHoneyComb type) {
        this.type = type;
    }

    public CosmicBeesHoneyComb getType() {
        return type;
    }

    @Override
    public int getColorFromItemStack(ItemStack itemstack, int tintIndex) {
        CosmicBeesHoneyComb honeyComb = this.type;

        if (tintIndex == 1) {
            return honeyComb.primaryColor;
        } else {
            return honeyComb.secondaryColor;
        }
    }
}
