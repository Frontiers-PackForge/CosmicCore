package com.ghostipedia.cosmiccore.common.food;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record AttributeSpec(Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation) {}
