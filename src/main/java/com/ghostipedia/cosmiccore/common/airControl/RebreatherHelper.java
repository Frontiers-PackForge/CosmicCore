package com.ghostipedia.cosmiccore.common.airControl;

import com.ghostipedia.cosmiccore.common.data.CosmicItems;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.simibubi.create.content.equipment.armor.DivingHelmetItem;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Optional;

/**
 * Helper class for detecting and interacting with rebreather equipment.
 */
public final class RebreatherHelper {

    private RebreatherHelper() {}

    /**
     * Rebreather types in order of capability.
     */
    public enum RebreatherType {
        /** No rebreather equipped */
        NONE,
        /** Simple rebreather - slows oxygen drain in THIN air only */
        SIMPLE,
        /** Pressurized rebreather - works in NO_AIR, allows tank usage */
        PRESSURIZED
    }

    /**
     * Get the best rebreather type the player has equipped.
     * Checks head curio slot for rebreather items.
     *
     * @param player The player to check
     * @return The best rebreather type found
     */
    public static RebreatherType getEquippedRebreather(Player player) {
        if (player == null) return RebreatherType.NONE;

        // Check for pressurized first (better)
        if (hasCurioItem(player, "head", CosmicItems.PRESSURIZED_REBREATHER.asItem())) {
            return RebreatherType.PRESSURIZED;
        }

        if (hasCreateDivingHelmet(player)) {
            return RebreatherType.PRESSURIZED;
        }

        // Check for simple
        if (hasCurioItem(player, "head", CosmicItems.SIMPLE_REBREATHER.asItem())) {
            return RebreatherType.SIMPLE;
        }

        return RebreatherType.NONE;
    }

    public static boolean hasCreateDivingHelmet(Player player) {
        return player != null && player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof DivingHelmetItem;
    }

    /**
     * Check if player has a simple rebreather or better equipped.
     */
    public static boolean hasSimpleRebreatherOrBetter(Player player) {
        RebreatherType type = getEquippedRebreather(player);
        return type == RebreatherType.SIMPLE || type == RebreatherType.PRESSURIZED;
    }

    /**
     * Check if player has a pressurized rebreather equipped.
     */
    public static boolean hasPressurizedRebreather(Player player) {
        return getEquippedRebreather(player) == RebreatherType.PRESSURIZED;
    }

    /**
     * Check if a specific item is in a curio slot.
     */
    private static boolean hasCurioItem(LivingEntity entity, String slotId, Item item) {
        Optional<ICuriosItemHandler> cap = CuriosApi.getCuriosInventory(entity);
        if (cap.isPresent()) {
            ICuriosItemHandler curioHandler = cap.get();
            Optional<ICurioStacksHandler> handler = curioHandler.getStacksHandler(slotId);
            if (handler.isPresent()) {
                IDynamicStackHandler stackHandler = handler.get().getStacks();
                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    ItemStack stack = stackHandler.getStackInSlot(i);
                    if (stack.is(item)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
