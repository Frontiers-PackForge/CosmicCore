package com.ghostipedia.cosmiccore.common.item.behavior;

import com.ghostipedia.cosmiccore.common.data.CosmicItems;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

public class GravityCoreBehavior {

    public static float clampGravity(Entity entity, float gravity) {
        if (entity instanceof Player player) {
            LazyOptional<ICuriosItemHandler> curiosInv = CuriosApi.getCuriosInventory(player);
            // Check the Inventory and Curio Slots for the GravCore
            var playerInventory = player.getInventory();
            IItemHandler curiosInventory = CuriosApi.getCuriosInventory(player)
                    .<IItemHandler>map(ICuriosItemHandler::getEquippedCurios)
                    .orElse(EmptyHandler.INSTANCE);
            for (int i = 0; i < playerInventory.getContainerSize(); i++) {
                var item = playerInventory.getItem(i);
                if (item.is(CosmicItems.PORTABLE_GRAVITY_CORE.asItem())) {
                    if (gravity != 1.0F) {
                        return 1.0F;
                    }
                }
            }
            for (int i = 0; i < curiosInventory.getSlots(); i++) {
                var curio = curiosInventory.getStackInSlot(i);
                if (curio.is(CosmicItems.PORTABLE_GRAVITY_CORE.asItem())) {
                    if (gravity != 1.0F) {
                        return 1.0F;
                    }
                }
            }
        }

        return gravity;
    }
}
