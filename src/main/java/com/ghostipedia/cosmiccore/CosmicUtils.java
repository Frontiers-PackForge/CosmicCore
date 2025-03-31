package com.ghostipedia.cosmiccore;

import com.ghostipedia.cosmiccore.common.data.CosmicItems;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;

import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Optional;

public class CosmicUtils {

    public static boolean hasTheOneRing(@Nullable Entity entity) {
        return hasCurio(entity, "ring", CosmicItems.THE_ONE_RING.asItem());
    }

    /**
     * Check if an entity has a specific item in a curio slot
     * 
     * @param entity    Entity to check
     * @param curioSlot Curio slot to check for item
     * @param item      Item to check for in curio slot
     * @return True if item was found
     */
    public static boolean hasCurio(@Nullable Entity entity, String curioSlot, Item item) {
        if (!(entity instanceof LivingEntity living)) {
            return false;
        }

        LazyOptional<ICuriosItemHandler> cap = CuriosApi.getCuriosInventory(living);
        if (cap.isPresent()) {
            ICuriosItemHandler curioHandler = cap.resolve().get();
            Optional<ICurioStacksHandler> handler = curioHandler.getStacksHandler(curioSlot);
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
