package com.ghostipedia.cosmiccore.common.item.armor;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.common.item.armor.QuarkTechSuite;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class HelmetSanguineWarptechSuite extends QuarkTechSuite {

    public HelmetSanguineWarptechSuite(ArmorItem.Type slot, int energyPerUse, long capacity, int tier) {
        super(slot, energyPerUse, capacity, tier);
    }

    @Override
    public void onArmorTick(Level world, Player player, ItemStack itemStack) {
        super.onArmorTick(world, player, itemStack);
        if (world.isClientSide) return;
        int foodLevel = player.getFoodData().getFoodLevel();
        if (foodLevel < 20) {
            player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 1, 1, false, false));
        }
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
                                            ArmorMaterial.Layer layer) {
        return CosmicCore.id("textures/armor/sanguine_suit_1.png");
    }
}
