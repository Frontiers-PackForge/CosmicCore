package com.ghostipedia.cosmiccore.common.compat.gtceu;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.armor.ModularArmorItem;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import com.mojang.serialization.Codec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public final class LegacyArmorAttributeMigration {

    private static final Map<ResourceLocation, Signature> LEGACY_MODIFIERS = legacyModifiers();

    private LegacyArmorAttributeMigration() {}

    public static Codec<ItemStack> wrap(Codec<ItemStack> codec) {
        return codec.xmap(LegacyArmorAttributeMigration::migrate, Function.identity());
    }

    public static ItemStack migrate(ItemStack stack) {
        if (!(stack.getItem() instanceof ModularArmorItem armor)) return stack;
        var saved = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (saved == null) return stack;
        var slot = EquipmentSlotGroup.bySlot(armor.getEquipmentSlot());
        var retained = saved.modifiers().stream().filter(entry -> !isLegacy(entry, slot)).toList();
        if (retained.size() == saved.modifiers().size()) return stack;

        stack.remove(DataComponents.ATTRIBUTE_MODIFIERS);
        if (retained.isEmpty()) return stack;

        var entries = new ArrayList<>(retained);
        for (var entry : armor.getDefaultAttributeModifiers(stack).modifiers()) {
            boolean overridden = retained.stream()
                    .anyMatch(existing -> existing.attribute().equals(entry.attribute()) &&
                            existing.modifier().id().equals(entry.modifier().id()));
            if (!overridden) entries.add(entry);
        }
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, new ItemAttributeModifiers(entries, saved.showInTooltip()));
        return stack;
    }

    private static boolean isLegacy(ItemAttributeModifiers.Entry entry, EquipmentSlotGroup slot) {
        var signature = LEGACY_MODIFIERS.get(entry.modifier().id());
        return signature != null && entry.slot() == slot && entry.attribute().is(signature.attribute()) &&
                entry.modifier().operation() == signature.operation();
    }

    private static Map<ResourceLocation, Signature> legacyModifiers() {
        var tiered = Map.of(
                "attack_speed",
                new Signature("minecraft:generic.attack_speed", AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                "attack_damage",
                new Signature("minecraft:generic.attack_damage", AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                "block_reach",
                new Signature("minecraft:player.block_interaction_range", AttributeModifier.Operation.ADD_VALUE),
                "movement_speed",
                new Signature("minecraft:generic.movement_speed", AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                "step_height", new Signature("minecraft:generic.step_height", AttributeModifier.Operation.ADD_VALUE),
                "swim_speed", new Signature("neoforge:swim_speed", AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        Map<ResourceLocation, Signature> result = new HashMap<>();
        for (int tier = GTValues.LV; tier <= GTValues.OpV; tier++) {
            String prefix = GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_";
            tiered.forEach((name, signature) -> result
                    .put(ResourceLocation.fromNamespaceAndPath("gtceu", prefix + name), signature));
        }
        result.put(ResourceLocation.parse("gtceu:creative_flight"),
                new Signature("neoforge:creative_flight", AttributeModifier.Operation.ADD_VALUE));
        return Map.copyOf(result);
    }

    private record Signature(ResourceLocation attribute, AttributeModifier.Operation operation) {

        private Signature(String attribute, AttributeModifier.Operation operation) {
            this(ResourceLocation.parse(attribute), operation);
        }
    }
}
