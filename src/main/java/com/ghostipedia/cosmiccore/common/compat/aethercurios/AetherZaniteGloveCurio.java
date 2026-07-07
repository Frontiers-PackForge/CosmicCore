package com.ghostipedia.cosmiccore.common.compat.aethercurios;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class AetherZaniteGloveCurio extends AetherGloveCurio {

    public AetherZaniteGloveCurio(ResourceLocation equipSound) {
        super(0.5, equipSound);
    }

    @Override
    protected double punchDamage(ItemStack stack) {
        int max = stack.getMaxDamage();
        int remaining = max - stack.getDamageValue();
        if (remaining >= max - (int) (max / 4.0)) return 0.25;
        if (remaining >= max - (int) (max / 1.5)) return 0.5;
        return 0.75;
    }
}
