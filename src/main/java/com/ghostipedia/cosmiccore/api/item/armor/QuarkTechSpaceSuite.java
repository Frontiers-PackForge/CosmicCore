package com.ghostipedia.cosmiccore.api.item.armor;

import com.gregtechceu.gtceu.common.item.armor.QuarkTechSuite;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class QuarkTechSpaceSuite extends QuarkTechSuite implements ISpaceSuite {

    public QuarkTechSpaceSuite(ArmorItem.Type slot, int energyPerUse, long capacity, int tier) {
        super(slot, energyPerUse, capacity, tier);
    }

    @Override
    public void onArmorTick(Level world, Player player, ItemStack itemStack) {
        super.onArmorTick(world, player, itemStack);
        onArmorTick(world, player, itemStack, type);
    }

    @Override
    public void addInfo(ItemStack itemStack, List<Component> lines) {
        super.addInfo(itemStack, lines);
        addInfo(itemStack, lines, type);
    }
}
