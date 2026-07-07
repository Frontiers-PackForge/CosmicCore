package com.ghostipedia.cosmiccore.common.compat.aethercurios;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import top.theillusivec4.curios.api.SlotContext;

public class AetherAgilityCapeCurio extends AetherAccessoryCurio {

    private static final ResourceLocation STEP_HEIGHT_ID = ResourceLocation.fromNamespaceAndPath("aether",
            "agility_cape_step_height");

    public AetherAgilityCapeCurio(ResourceLocation equipSound) {
        super(equipSound);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        AttributeInstance stepHeight = entity.getAttribute(Attributes.STEP_HEIGHT);
        if (stepHeight == null) return;
        if (entity.isShiftKeyDown()) {
            stepHeight.removeModifier(STEP_HEIGHT_ID);
        } else if (stepHeight.getModifier(STEP_HEIGHT_ID) == null) {
            stepHeight.addTransientModifier(
                    new AttributeModifier(STEP_HEIGHT_ID, 0.5, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        AttributeInstance stepHeight = slotContext.entity().getAttribute(Attributes.STEP_HEIGHT);
        if (stepHeight != null) {
            stepHeight.removeModifier(STEP_HEIGHT_ID);
        }
    }
}
