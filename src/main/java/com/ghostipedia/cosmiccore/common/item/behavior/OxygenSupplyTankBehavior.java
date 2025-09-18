package com.ghostipedia.cosmiccore.common.item.behavior;

import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.ghostipedia.cosmiccore.common.airControl.IOxygenSupplyItem;
import com.ghostipedia.cosmiccore.common.airControl.OxygenItemCap;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import org.jetbrains.annotations.NotNull;

public class OxygenSupplyTankBehavior implements IItemComponent, IComponentCapability {

    private static final String TAG_ROOT = "CosmicCoreO2";
    private static final String TAG_BUF  = "TickBuffer";      // stored oxygen ticks
    private static final String TAG_CAP  = "CapacityMb";      // for tooltip
    private static final String TAG_TPT  = "TransferPerTick"; // for tooltip
    private static final String TAG_TPM  = "TicksPerMb";      // for tooltip

    @Getter
    private final int capacityMb;
    @Getter
    private final int transferPerTick;     // max oxygen ticks we can output per game tick
    @Getter
    private final int ticksPerMb;          // 1 mB -> this many oxygen ticks

    public OxygenSupplyTankBehavior(int capacityMb, int transferPerTick, int ticksPerMb) {
        this.capacityMb = capacityMb;
        this.transferPerTick = Math.max(0, transferPerTick);
        this.ticksPerMb = Math.max(1, ticksPerMb);
    }

     //Create a buffer of ticks inside the item and only drain when that buffer is satisfied
    public int drainTicks(ItemStack stack, int requestTicks) {
        if (requestTicks <= 0) return 0;

        int outLimit = Math.min(requestTicks, transferPerTick);

        IFluidHandlerItem fluidHandler = getFluidHandler(stack);

        // Pull buffered ticks from NBT (how many oxygen ticks we have "ready")
        int buffer = getTickBuffer(stack);

        // If buffer can't satisfy the output limit, top-up by draining at most 1 mB this call.
        if (buffer < outLimit) {
            // Drain exactly 1 mB (if available) and convert to ticks
            FluidStack drained = fluidHandler.drain(new FluidStack(GTMaterials.Oxygen.getFluid(), 1), IFluidHandlerItem.FluidAction.EXECUTE);
            if (!drained.isEmpty()) {
                buffer += drained.getAmount() * ticksPerMb; // amount will be 1 mB here
            }
        }

        // Provide ticks out of the buffer (never exceeding outLimit)
        int provided = Math.min(outLimit, buffer);
        if (provided > 0) {
            setTickBuffer(stack, buffer - provided);
        }
        return provided;
    }

    private IFluidHandlerItem getFluidHandler(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
    }

    // ---- NBT helpers ----
    private int getTickBuffer(ItemStack stack) {
        CompoundTag compoundTag = stack.getOrCreateTag().getCompound(TAG_ROOT);
        return compoundTag.getInt(TAG_BUF);
    }

    private void setTickBuffer(ItemStack stack, int value) {
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag compoundTag  = tag.getCompound(TAG_ROOT);
        compoundTag.putInt(TAG_BUF, Math.max(0, value));
        tag.put(TAG_ROOT, compoundTag);
    }

    //Values are written so tooltips can read them.
    private void ensureConfigWritten(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag compoundTag  = tag.getCompound(TAG_ROOT);
        compoundTag.putInt(TAG_CAP, capacityMb);
        compoundTag.putInt(TAG_TPT, transferPerTick);
        compoundTag.putInt(TAG_TPM, ticksPerMb);
        tag.put(TAG_ROOT, compoundTag);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(ItemStack stack, @NotNull Capability<T> cap) {
        //Set our NBT
        ensureConfigWritten(stack);

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
