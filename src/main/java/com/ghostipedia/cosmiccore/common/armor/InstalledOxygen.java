package com.ghostipedia.cosmiccore.common.armor;

import com.ghostipedia.cosmiccore.common.item.behavior.OxygenSupplyTankBehavior;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.armor.ModularArmorItem;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class InstalledOxygen {

    private InstalledOxygen() {}

    public static boolean hasTank(Player player) {
        var chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof ModularArmorItem armor) || armor.getEquipmentSlot() != EquipmentSlot.CHEST)
            return false;
        var modular = GTCapabilityHelper.getModularItem(chest);
        return modular != null && modular.getAllModuleInstances().stream()
                .anyMatch(context -> context.getModule() instanceof OxygenTankModule);
    }

    private static ModuleContext tank(ItemStack chest) {
        if (!(chest.getItem() instanceof ModularArmorItem armor) || armor.getEquipmentSlot() != EquipmentSlot.CHEST)
            return null;
        var modular = GTCapabilityHelper.getModularItem(chest);
        if (modular == null) return null;
        return modular.getAllModuleInstances().stream()
                .filter(context -> context.getModule() instanceof OxygenTankModule && context.getData().isEnabled())
                .findFirst().orElse(null);
    }

    public static int drain(Player player, int ticks) {
        return drain(player.getItemBySlot(EquipmentSlot.CHEST), ticks);
    }

    public static int drain(ItemStack chest, int ticks) {
        var context = tank(chest);
        if (context == null || ticks <= 0) return 0;
        var item = context.getData().getModuleItem().copy();
        int drained = ((OxygenTankModule) context.getModule()).tank().drainTicks(item, ticks);
        if (drained > 0) context.setData(context.getData().withModuleItem(item));
        return drained;
    }

    public static long remaining(Player player) {
        return remaining(player.getItemBySlot(EquipmentSlot.CHEST));
    }

    public static long remaining(ItemStack chest) {
        var context = tank(chest);
        return context == null ? 0 : OxygenSupplyTankBehavior.remainingTicks(context.getData().getModuleItem());
    }
}
