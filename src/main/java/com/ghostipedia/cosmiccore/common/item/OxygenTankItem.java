package com.ghostipedia.cosmiccore.common.item;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;

public class OxygenTankItem extends ComponentItem {
    public OxygenTankItem(Properties props) { super(props); }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        IFluidHandlerItem fluidHandlerItem = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        int amount = fluidHandlerItem.getFluidInTank(0).getAmount();
        int cap = Math.max(1, fluidHandlerItem.getTankCapacity(0));
        return Math.round(13.0f * amount / cap);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return 0x55D8FF;
    }
}
