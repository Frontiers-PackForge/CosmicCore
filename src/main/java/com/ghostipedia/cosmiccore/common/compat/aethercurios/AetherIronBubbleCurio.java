package com.ghostipedia.cosmiccore.common.compat.aethercurios;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import top.theillusivec4.curios.api.SlotContext;

public class AetherIronBubbleCurio extends AetherAccessoryCurio {

    public AetherIronBubbleCurio(ResourceLocation equipSound) {
        super(equipSound);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity.level().isClientSide) return;
        if (entity.isUnderWater()) {
            entity.setAirSupply(30);
        }
    }
}
