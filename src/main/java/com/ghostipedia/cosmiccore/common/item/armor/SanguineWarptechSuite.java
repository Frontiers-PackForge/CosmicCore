package com.ghostipedia.cosmiccore.common.item.armor;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.common.item.armor.QuarkTechSuite;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

public class SanguineWarptechSuite extends QuarkTechSuite {

    public SanguineWarptechSuite(ArmorItem.Type slot, int energyPerUse, long capacity, int tier) {
        super(slot, energyPerUse, capacity, tier);
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
                                            ArmorMaterial.Layer layer) {
        return slot != EquipmentSlot.LEGS ?
                CosmicCore.id("textures/armor/sanguine_suit_1.png") :
                CosmicCore.id("textures/armor/sanguine_suit_2.png");
    }
}
