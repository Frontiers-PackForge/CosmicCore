package com.ghostipedia.cosmiccore.common.compat.aethercurios;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import top.theillusivec4.curios.api.SlotContext;

public class AetherSlowFallCurio extends AetherAccessoryCurio {

    public AetherSlowFallCurio(ResourceLocation equipSound) {
        super(equipSound);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        AttributeInstance gravity = entity.getAttribute(Attributes.GRAVITY);
        if (gravity == null) return;

        Vec3 delta = entity.getDeltaMovement();
        if (delta.y <= -0.06 && !entity.onGround() && !entity.isFallFlying() && !entity.isInFluidType() &&
                !entity.isShiftKeyDown() && gravity.getValue() > 0.0075) {
            entity.setDeltaMovement(delta.multiply(1.0, 0.6, 1.0));
        }
        if (entity.getDeltaMovement().y > -0.5) {
            entity.fallDistance = 1.0f;
        }
    }
}
