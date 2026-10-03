package com.ghostipedia.cosmiccore.common.compat.gtceu;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

public final class TurbineRotorImmortality {

    private TurbineRotorImmortality() {}

    public static void normalize(ItemStack stack) {
        stack.remove(DataComponents.DAMAGE);
        stack.remove(DataComponents.MAX_DAMAGE);
    }
}
