package com.ghostipedia.cosmiccore.common.compat.aethercurios;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import top.theillusivec4.curios.api.SlotContext;

public class AetherGloveCurio extends AetherAccessoryCurio {

    private final double punchDamage;

    public AetherGloveCurio(double punchDamage, ResourceLocation equipSound) {
        super(equipSound);
        this.punchDamage = punchDamage;
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext,
                                                                                ResourceLocation id,
                                                                                ItemStack stack) {
        Multimap<Holder<Attribute>, AttributeModifier> modifiers = LinkedHashMultimap.create();
        modifiers.put(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(id, punchDamage(stack), AttributeModifier.Operation.ADD_VALUE));
        return modifiers;
    }

    protected double punchDamage(ItemStack stack) {
        return punchDamage;
    }
}
