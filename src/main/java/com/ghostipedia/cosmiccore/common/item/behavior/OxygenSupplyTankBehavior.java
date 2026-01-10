package com.ghostipedia.cosmiccore.common.item.behavior;

import com.ghostipedia.cosmiccore.common.airControl.IOxygenSupplyItem;
import com.ghostipedia.cosmiccore.common.airControl.OxygenItemCap;

import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class OxygenSupplyTankBehavior implements IItemComponent, IComponentCapability {

    private static final String TAG_ROOT = "CosmicCoreO2";
    private static final String TAG_BUF = "TickBuffer";
    private static final String TAG_CAP = "CapacityMb";
    private static final String TAG_TPT = "TransferPerTick";
    private static final String TAG_TPM = "TicksPerMb";

    @Getter
    private final int capacityMb;
    @Getter
    private final int transferPerTick;
    @Getter
    private final int ticksPerMb;

    public OxygenSupplyTankBehavior(int capacityMb, int transferPerTick, int ticksPerMb) {
        this.capacityMb = capacityMb;
        this.transferPerTick = Math.max(0, transferPerTick);
        this.ticksPerMb = Math.max(1, ticksPerMb);
    }

    /**
     * Drains oxygen ticks from the tank's internal buffer, refilling from fluid as needed.
     */
    public int drainTicks(ItemStack stack, int requestTicks) {
        if (requestTicks <= 0) return 0;

        int outLimit = Math.min(requestTicks, transferPerTick);

        IFluidHandlerItem fluidHandler = getFluidHandler(stack);
        if (fluidHandler == null) return 0;

        int buffer = getTickBuffer(stack);

        // If buffer can't satisfy the output limit, top-up by draining 1 mB
        if (buffer < outLimit) {
            FluidStack drained = fluidHandler.drain(
                    new FluidStack(GTMaterials.Oxygen.getFluid(), 1),
                    IFluidHandlerItem.FluidAction.EXECUTE
            );
            if (!drained.isEmpty()) {
                buffer += drained.getAmount() * ticksPerMb;
            }
        }

        int provided = Math.min(outLimit, buffer);
        if (provided > 0) {
            setTickBuffer(stack, buffer - provided);
        }
        return provided;
    }

    private IFluidHandlerItem getFluidHandler(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
    }

    private int getTickBuffer(ItemStack stack) {
        CompoundTag compoundTag = stack.getOrCreateTag().getCompound(TAG_ROOT);
        return compoundTag.getInt(TAG_BUF);
    }

    private void setTickBuffer(ItemStack stack, int value) {
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag compoundTag = tag.getCompound(TAG_ROOT);
        compoundTag.putInt(TAG_BUF, Math.max(0, value));
        tag.put(TAG_ROOT, compoundTag);
    }

    private void ensureConfigWritten(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag compoundTag = tag.getCompound(TAG_ROOT);
        compoundTag.putInt(TAG_CAP, capacityMb);
        compoundTag.putInt(TAG_TPT, transferPerTick);
        compoundTag.putInt(TAG_TPM, ticksPerMb);
        tag.put(TAG_ROOT, compoundTag);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(ItemStack stack, @NotNull Capability<T> cap) {
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
            IOxygenSupplyItem provider = (stk, req) -> drainTicks(stk, req);
            return OxygenItemCap.OXYGEN_SUPPLY.orEmpty(cap, LazyOptional.of(() -> provider));
        }

        return LazyOptional.empty();
    }
}
