package com.ghostipedia.cosmiccore;

import com.ghostipedia.cosmiccore.api.item.component.ICustomRenderer;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.ComponentItem;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.tterrag.registrate.util.nullness.NonNullConsumer;
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

    public static <T extends ComponentItem> NonNullConsumer<T> attachRenderer(ICustomRenderer customRenderer) {
        return !GTCEu.isClientSide() ? NonNullConsumer.noop() : (item) -> item.attachComponents(customRenderer);
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

        Optional<ICuriosItemHandler> cap = CuriosApi.getCuriosInventory(living);
        if (cap.isPresent()) {
            ICuriosItemHandler curioHandler = cap.get();
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
