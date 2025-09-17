package com.ghostipedia.cosmiccore.common.item.behavior;

import com.ghostipedia.cosmiccore.common.airControl.IOxygenSupplyItem;
import com.ghostipedia.cosmiccore.common.airControl.OxygenItemCap;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import org.jetbrains.annotations.NotNull;

public class OxygenSupplyTankBehavior implements IItemComponent, IComponentCapability {

    private final int capacityMb;
    private final int transferPerTick;     // ticks per server tick the tank can output
    private final int ticksPerMb;          // conversion: 1 mB -> this many oxygen ticks

    public OxygenSupplyTankBehavior(int capacityMb, int transferPerTick, int ticksPerMb) {
        this.capacityMb = capacityMb;
        this.transferPerTick = transferPerTick;
        this.ticksPerMb = Math.max(1, ticksPerMb);
    }

    public int getCapacityMb()      { return capacityMb; }
    public int getTransferPerTick() { return transferPerTick; }
    public int getTicksPerMb()      { return ticksPerMb; }

    /** Provide up to requestTicks from the internal oxygen fluid, respecting transferPerTick. */
    public int drainTicks(ItemStack stack, int requestTicks) {
        if (requestTicks <= 0) return 0;
        int maxOut = Math.min(requestTicks, transferPerTick);

        IFluidHandlerItem h = getFluidHandler(stack);
        if (h == null) return 0;

        int needMb = Mth.ceil(maxOut / (double) ticksPerMb);
        FluidStack drained = h.drain(new FluidStack(GTMaterials.Oxygen.getFluid(), needMb), IFluidHandlerItem.FluidAction.EXECUTE);
        int providedTicks = drained.getAmount() * ticksPerMb;
        return Math.min(maxOut, providedTicks);
    }

    private IFluidHandlerItem getFluidHandler(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(ItemStack stack, @NotNull Capability<T> cap) {
        // Keep the original FLUID_HANDLER_ITEM exposure
        if (cap == ForgeCapabilities.FLUID_HANDLER_ITEM) {
            return ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty(cap, LazyOptional.of(() ->
                    new FluidHandlerItemStack(stack, capacityMb) {
                        @Override
                        public boolean isFluidValid(int tank, FluidStack fluidStack) {
                            return fluidStack.getFluid() == GTMaterials.Oxygen.getFluid();
                        }
                    }
            ));
        }

        // NEW: expose an oxygen-supply view for OxygenLogic to use
        if (cap == OxygenItemCap.OXYGEN_SUPPLY) {
            IOxygenSupplyItem provider = new IOxygenSupplyItem() {
                @Override
                public int drainOxygenTicks(ItemStack stk, int req) {
                    return drainTicks(stk, req);
                }
            };
            return OxygenItemCap.OXYGEN_SUPPLY.orEmpty(cap, LazyOptional.of(() -> provider));
        }

        return LazyOptional.empty();
    }
}
