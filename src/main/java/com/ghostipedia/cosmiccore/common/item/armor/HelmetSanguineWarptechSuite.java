package com.ghostipedia.cosmiccore.common.item.armor;

import com.gregtechceu.gtceu.common.item.armor.QuarkTechSuite;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class HelmetSanguineWarptechSuite extends QuarkTechSuite {

    public HelmetSanguineWarptechSuite(ArmorItem.Type slot, int energyPerUse, long capacity, int tier) {
        super(slot, energyPerUse, capacity, tier);
    }

    @Override
    public void onArmorTick(Level world, Player player, ItemStack itemStack) {
        super.onArmorTick(world, player, itemStack);
        if(world.isClientSide) return;
        int foodLevel = player.getFoodData().getFoodLevel();
        if(foodLevel < 20) {
            player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 1, 1, false, false));
        }
    }
}
