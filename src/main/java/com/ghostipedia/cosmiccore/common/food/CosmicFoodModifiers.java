package com.ghostipedia.cosmiccore.common.food;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CosmicFoodModifiers {

    private CosmicFoodModifiers() {}

    public static final double BASE_HEALTH_DELTA = -4.0;

    public static final ResourceLocation BASE_HEALTH_ID = CosmicCore.id("food_base_health");
    public static final ResourceLocation FOOD_HEALTH_ID = CosmicCore.id("food_health_bonus");

    public static boolean applyMaxHealth(Player player, double foodBonus) {
        AttributeInstance attr = player.getAttribute(Attributes.MAX_HEALTH);
        if (attr == null) return false;

        if (attr.getModifier(BASE_HEALTH_ID) == null) {
            attr.addTransientModifier(
                    new AttributeModifier(BASE_HEALTH_ID, BASE_HEALTH_DELTA, AttributeModifier.Operation.ADD_VALUE));
        }

        AttributeModifier current = attr.getModifier(FOOD_HEALTH_ID);
        if (current == null || current.amount() != foodBonus) {
            attr.addOrUpdateTransientModifier(
                    new AttributeModifier(FOOD_HEALTH_ID, foodBonus, AttributeModifier.Operation.ADD_VALUE));
        }
        if (player.getHealth() <= player.getMaxHealth()) return false;
        player.setHealth(player.getMaxHealth());
        return true;
    }

    public static void applyAttributeModifiers(Player player, List<AttributeSpec> specs, CosmicFoodData data) {
        Map<ResourceLocation, AttributeSpec> aggregated = new LinkedHashMap<>();
        for (AttributeSpec spec : specs) {
            ResourceLocation id = attrModId(spec.attribute(), spec.operation());
            AttributeSpec prev = aggregated.get(id);
            double amount = spec.amount() + (prev != null ? prev.amount() : 0.0);
            aggregated.put(id, new AttributeSpec(spec.attribute(), amount, spec.operation()));
        }

        Set<ResourceLocation> active = new HashSet<>();
        for (Map.Entry<ResourceLocation, AttributeSpec> entry : aggregated.entrySet()) {
            AttributeInstance attr = player.getAttribute(entry.getValue().attribute());
            if (attr == null) continue;
            attr.addOrUpdateTransientModifier(new AttributeModifier(entry.getKey(),
                    entry.getValue().amount(), entry.getValue().operation()));
            active.add(entry.getKey());
            data.appliedAttrMods.put(entry.getKey(), entry.getValue().attribute());
        }

        Iterator<Map.Entry<ResourceLocation, Holder<Attribute>>> it = data.appliedAttrMods.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ResourceLocation, Holder<Attribute>> entry = it.next();
            if (!active.contains(entry.getKey())) {
                AttributeInstance attr = player.getAttribute(entry.getValue());
                if (attr != null) attr.removeModifier(entry.getKey());
                it.remove();
            }
        }
    }

    private static ResourceLocation attrModId(Holder<Attribute> attribute, AttributeModifier.Operation op) {
        ResourceLocation loc = attribute.unwrapKey().map(ResourceKey::location)
                .orElseGet(() -> CosmicCore.id("unknown"));
        return CosmicCore.id("food_attr." + loc.getNamespace() + "." + loc.getPath() + "." + op.ordinal());
    }
}
