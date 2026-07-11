package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;

public final class CosmicArmorMaterials {

    private CosmicArmorMaterials() {}

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
}
