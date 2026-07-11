package com.ghostipedia.cosmiccore.common.item;

import com.ghostipedia.cosmiccore.common.item.behavior.OxygenSupplyTankBehavior;

import com.gregtechceu.gtceu.api.item.ComponentItem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OxygenTankItem extends ComponentItem {

    public OxygenTankItem(Properties props) {
        super(props);
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        IFluidHandlerItem h = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (h == null) return 0;
        int amount = h.getFluidInTank(0).getAmount();
        int cap = Math.max(1, h.getTankCapacity(0));
        return Math.round(13.0f * amount / cap);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return 0x55D8FF;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.TooltipContext context,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        IFluidHandlerItem h = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (h == null) return;

        int amt = h.getFluidInTank(0).getAmount();
        int cap = h.getTankCapacity(0);
        tooltip.add(Component.translatable("cosmiccore.tooltip.oxygen_tank.fill", amt, cap)
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("cosmiccore.tooltip.oxygen_tank.runtime",
                formatDurationSeconds(OxygenSupplyTankBehavior.remainingTicks(stack) / 20.0))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("cosmiccore.tooltip.oxygen_tank.rebreather")
                .withStyle(ChatFormatting.RED));
    }

    private static String formatDurationSeconds(double seconds) {
        if (seconds < 60) return String.format("%.1fs", seconds);
        int s = (int) Math.floor(seconds);
        int m = s / 60;
        int r = s % 60;
        return String.format("%dm %02ds", m, r);
    }
}
