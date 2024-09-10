package com.ghostipedia.cosmiccore;

import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Optional;

public class CosmicUtils {

    public static boolean hasRing(@Nullable Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return false;
        }

        LazyOptional<ICuriosItemHandler> cap = CuriosApi.getCuriosInventory(living);
        if (cap.isPresent()) {
            ICuriosItemHandler curioHandler = cap.resolve().get();
            Optional<ICurioStacksHandler> handler = curioHandler.getStacksHandler("ring");
            if (handler.isPresent()) {
                IDynamicStackHandler stackHandler = handler.get().getStacks();
                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    ItemStack stack = stackHandler.getStackInSlot(i);
                    if (stack.is(CosmicItems.THE_ONE_RING.get())) {
                        return true;
                    }
                }
            }
        }
        return false;

    }

}
