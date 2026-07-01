package com.ghostipedia.cosmiccore.common.food;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class CosmicFoodModifiers {

    private CosmicFoodModifiers() {}

    public static final double BASE_HEALTH_DELTA = -4.0;

    public static final ResourceLocation BASE_HEALTH_ID = CosmicCore.id("food_base_health");
    public static final ResourceLocation FOOD_HEALTH_ID = CosmicCore.id("food_health_bonus");

    public static void applyMaxHealth(Player player, double foodBonus) {
        AttributeInstance attr = player.getAttribute(Attributes.MAX_HEALTH);
        if (attr == null) return;

        if (attr.getModifier(BASE_HEALTH_ID) == null) {
            attr.addTransientModifier(
                    new AttributeModifier(BASE_HEALTH_ID, BASE_HEALTH_DELTA, AttributeModifier.Operation.ADD_VALUE));
        }

        AttributeModifier current = attr.getModifier(FOOD_HEALTH_ID);
        if (current == null || current.amount() != foodBonus) {
            attr.addOrUpdateTransientModifier(
                    new AttributeModifier(FOOD_HEALTH_ID, foodBonus, AttributeModifier.Operation.ADD_VALUE));
        }
    }
}
