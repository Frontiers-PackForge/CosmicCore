package com.ghostipedia.cosmiccore.common.item;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.ghostipedia.cosmiccore.common.airControl.OxygenItemCap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OxygenTankItem extends ComponentItem {
    public OxygenTankItem(Properties props) { super(props); }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        IFluidHandlerItem h = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        if (h == null) return 0;
        int amount = h.getFluidInTank(0).getAmount();
        int cap = Math.max(1, h.getTankCapacity(0));
        return Math.round(13.0f * amount / cap);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return 0x55D8FF;
    }

    // Tooltip and Processing the Tank Stats
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        IFluidHandlerItem h = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        int amt = 0, cap = 0;
        if (h != null) {
            amt = h.getFluidInTank(0).getAmount();
            cap = h.getTankCapacity(0);
            tooltip.add(line("Oxygen", amt + " / " + cap + " mB", ChatFormatting.AQUA));
        }

        // Read tuning written by the behavior into NBT (always present after first capability touch)
        var tag = stack.getOrCreateTag().getCompound("CosmicCoreO2");
        int ticksPerMb = tag.getInt("TicksPerMb");
        int transferPerTick = tag.getInt("TransferPerTick");

        // Fallbacks (just in case this stack hasn't been touched by the capability yet)
        if (ticksPerMb <= 0 || transferPerTick < 0) {
            // Try to nudge the capability so behavior writes the config, then read again
            stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
            tag = stack.getOrCreateTag().getCompound("CosmicCoreO2");
            ticksPerMb = Math.max(1, tag.getInt("TicksPerMb"));
            transferPerTick = Math.max(0, tag.getInt("TransferPerTick"));
        }

        if (ticksPerMb > 0) {
            // Max O2 Out
            int tpt = transferPerTick;
            int ticksPerSec = tpt * 20;

            // Fluid use at max, respecting the buffer behavior
            double mbPerTickAtMax = Math.min(1.0, tpt / (double) ticksPerMb);
            double mbPerSecAtMax  = mbPerTickAtMax * 20.0;

            tooltip.add(line("Max Output", tpt + " O₂/t (" + ticksPerSec + "/s)", ChatFormatting.GRAY));
            tooltip.add(line("Conversion", ticksPerMb + " O₂-ticks per mB", ChatFormatting.GRAY));
            tooltip.add(line("Use @ Max", fmt(mbPerTickAtMax) + " mB/t (" + fmt(mbPerSecAtMax) + " mB/s)", ChatFormatting.DARK_GRAY));

            if (cap > 0 && tpt > 0) {
                long totalOTicks = (long) amt * (long) ticksPerMb;
                long runGTicks = (long) Math.floor(totalOTicks / (double) tpt);
                tooltip.add(line("Est. Runtime @ Max", formatDurationSeconds(runGTicks / 20.0), ChatFormatting.DARK_GREEN));
            }
        }
    }

    private static Component line(String label, String value, ChatFormatting color) {
        return Component.literal(label + ": ").withStyle(ChatFormatting.WHITE)
                .append(Component.literal(value).withStyle(color));
    }

    private static String fmt(double d) {
        if (d >= 10) return String.format("%.0f", d);
        if (d >= 1)  return String.format("%.2f", d);
        return String.format("%.3f", d);
    }

    private static String formatDurationSeconds(double seconds) {
        if (seconds < 60) return String.format("%.1fs", seconds);
        int s = (int) Math.floor(seconds);
        int m = s / 60;
        int r = s % 60;
        return String.format("%dm %02ds", m, r);
    }
}
