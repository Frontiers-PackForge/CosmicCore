package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public final class CosmicArmorMaterials {

    private CosmicArmorMaterials() {}

    private static final TagKey<Item> STEEL_INGOTS = TagKey.create(
            Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "ingots/steel"));

    public static final Holder<ArmorMaterial> STEEL_TRAVELER = travelerMaterial(
            3, 10, SoundEvents.ARMOR_EQUIP_IRON, () -> Ingredient.of(STEEL_INGOTS), 2.0F, 0.0F);
    public static final Holder<ArmorMaterial> NETHERITE_TRAVELER = travelerMaterial(
            3, 15, SoundEvents.ARMOR_EQUIP_NETHERITE, () -> Ingredient.of(Items.NETHERITE_INGOT), 3.0F, 0.1F);
    public static final Holder<ArmorMaterial> NANO_TRAVELER = travelerMaterial(
            4, 18, SoundEvents.ARMOR_EQUIP_NETHERITE, () -> Ingredient.of(Items.NETHERITE_INGOT), 4.0F, 0.15F);
    public static final Holder<ArmorMaterial> QUARK_TRAVELER = travelerMaterial(
            4, 20, SoundEvents.ARMOR_EQUIP_NETHERITE, () -> Ingredient.of(Items.NETHERITE_INGOT), 5.0F, 0.2F);

    // A touch above netherite (3/8/6/3, toughness 3.0, kb 0.1): +1 defense on chest and legs (20 -> 22 armor),
    // toughness 4.0, knockback 0.15, enchantability 18. Worn skin = cosmiccore:shadebloom_layer_1/2.
    public static final Holder<ArmorMaterial> SHADEBLOOM = Holder.direct(new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 3);
                map.put(ArmorItem.Type.LEGGINGS, 7);
                map.put(ArmorItem.Type.CHESTPLATE, 9);
                map.put(ArmorItem.Type.HELMET, 3);
            }),
            18,
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            () -> Ingredient.of(Items.NETHERITE_INGOT),
            List.of(new ArmorMaterial.Layer(CosmicCore.id("shadebloom"))),
            4.0F,
            0.15F));

    private static Holder<ArmorMaterial> travelerMaterial(int defense, int enchantment,
                                                          Holder<net.minecraft.sounds.SoundEvent> equipSound,
                                                          Supplier<Ingredient> repairIngredient, float toughness,
                                                          float knockbackResistance) {
        return Holder.direct(new ArmorMaterial(
                Util.make(new EnumMap<>(ArmorItem.Type.class), map -> map.put(ArmorItem.Type.BOOTS, defense)),
                enchantment,
                equipSound,
                repairIngredient,
                List.of(new ArmorMaterial.Layer(CosmicCore.id("travelers_boots"))),
                toughness,
                knockbackResistance));
    }
}
