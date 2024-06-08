package com.ghostipedia.cosmiccore.api.item.armor;

import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.util.Lazy;

import com.mojang.serialization.Codec;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.EnumMap;

@AllArgsConstructor
public enum CCArmorMaterials implements ArmorMaterial, StringRepresentable {

    REBREATHER_EQUIPMENT("good_ppe_equipment", 20, Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
        map.put(ArmorItem.Type.BOOTS, 2);
        map.put(ArmorItem.Type.LEGGINGS, 5);
        map.put(ArmorItem.Type.CHESTPLATE, 6);
        map.put(ArmorItem.Type.HELMET, 2);
    }), 10, SoundEvents.ARMOR_EQUIP_ELYTRA, 0.0F, 0.0F, () -> Ingredient.EMPTY),
    ;

    public static final Codec<CCArmorMaterials> CODEC = StringRepresentable.fromEnum(CCArmorMaterials::values);
    private static final EnumMap<ArmorItem.Type, Integer> HEALTH_FUNCTION_FOR_TYPE = Util
            .make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 13);
                map.put(ArmorItem.Type.LEGGINGS, 15);
                map.put(ArmorItem.Type.CHESTPLATE, 16);
                map.put(ArmorItem.Type.HELMET, 11);
            });

    private final String name;
    private final int durabilityMultiplier;
    private final EnumMap<ArmorItem.Type, Integer> protectionFunctionForType;
    @Getter
    private final int enchantmentValue;
    @Getter
    private final SoundEvent equipSound;
    @Getter
    private final float toughness;
    @Getter
    private final float knockbackResistance;
    private final Lazy<Ingredient> repairIngredient;

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return HEALTH_FUNCTION_FOR_TYPE.get(type) * this.durabilityMultiplier;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return this.protectionFunctionForType.get(type);
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
