package com.ghostipedia.cosmiccore.common.armor;

import com.ghostipedia.cosmiccore.common.item.armor.boots.TravelerBootsItem;
import com.ghostipedia.cosmiccore.common.item.behavior.WirelessPDABehavior;

import com.gregtechceu.gtceu.api.item.armor.ModularArmorItem;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ModuleData;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

public final class ArmorModuleHelper {

    private ArmorModuleHelper() {}

    public static int tier(LivingEntity entity, EquipmentSlot slot, AbyssModule.Role role) {
        return tier(entity.getItemBySlot(slot), slot, role);
    }

    public static int tier(ItemStack stack, EquipmentSlot slot, AbyssModule.Role role) {
        if (!(stack.getItem() instanceof ModularArmorItem armor) || armor.getEquipmentSlot() != slot) return 0;
        var data = stack.get(GTDataComponents.MODULAR_ITEM_DATA);
        if (data == null) return 0;
        int result = 0;
        for (var installed : data.getModules()) {
            ItemModule module = installed.getModule();
            if (module instanceof AbyssModule abyss && abyss.role() == role && abyss.slot() == slot &&
                    installed.isEnabled()) {
                result = Math.max(result, abyss.tier());
            }
        }
        return result;
    }

    public static ItemStack installed(ItemStack armorStack, UtilityArmorModule.Role role) {
        if (!(armorStack.getItem() instanceof ModularArmorItem armor) || armor.getEquipmentSlot() != role.slot()) {
            return ItemStack.EMPTY;
        }
        var data = armorStack.get(GTDataComponents.MODULAR_ITEM_DATA);
        if (data == null) return ItemStack.EMPTY;
        return data.getModules().stream()
                .filter(module -> module.getModule() instanceof UtilityArmorModule utility && utility.role() == role)
                .filter(module -> module.isEnabled() && role.accepts(module.getModuleItem()))
                .min(Comparator.comparingInt(ModuleData::getSlot))
                .map(ModuleData::getModuleItem).orElse(ItemStack.EMPTY);
    }

    public static ItemStack activePDA(Player player) {
        if (player == null) return ItemStack.EMPTY;
        return selectPDA(WirelessPDABehavior.CosmicCuriosUtils.getPDACurio(player),
                player.getItemBySlot(EquipmentSlot.HEAD));
    }

    public static ItemStack selectPDA(ItemStack equippedCurio, ItemStack equippedHelmet) {
        return WirelessPDABehavior.isPDA(equippedCurio) ? equippedCurio :
                installed(equippedHelmet, UtilityArmorModule.Role.PDA);
    }

    public static boolean protectsFromFall(ItemStack equippedBoots) {
        return equippedBoots.getItem() instanceof TravelerBootsItem ||
                !installed(equippedBoots, UtilityArmorModule.Role.TRAVELER_BOOTS).isEmpty();
    }
}
