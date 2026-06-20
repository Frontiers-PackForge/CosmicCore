package com.ghostipedia.cosmiccore.common.item.behavior;

import com.ghostipedia.cosmiccore.common.airControl.IOxygenSupplyItem;
import com.ghostipedia.cosmiccore.common.airControl.OxygenItemCap;
import com.ghostipedia.cosmiccore.utils.ItemData;

import com.gregtechceu.gtceu.api.item.component.IComponentCapability;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;

import lombok.Getter;

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

        if (buffer < outLimit) {
            FluidStack drained = fluidHandler.drain(
                    new FluidStack(GTMaterials.Oxygen.getFluid(), 1),
                    IFluidHandlerItem.FluidAction.EXECUTE);
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
        return stack.getCapability(Capabilities.FluidHandler.ITEM);
    }

    private int getTickBuffer(ItemStack stack) {
        return ItemData.readElement(stack, TAG_ROOT).getInt(TAG_BUF);
    }

    private void setTickBuffer(ItemStack stack, int value) {
        ItemData.mutateElement(stack, TAG_ROOT, tag -> tag.putInt(TAG_BUF, Math.max(0, value)));
    }

    private void ensureConfigWritten(ItemStack stack) {
        ItemData.mutateElement(stack, TAG_ROOT, tag -> {
            tag.putInt(TAG_CAP, capacityMb);
            tag.putInt(TAG_TPT, transferPerTick);
            tag.putInt(TAG_TPM, ticksPerMb);
        });
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event, Item item) {
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> {
            ensureConfigWritten(stack);
            return new FluidHandlerItemStack(GTDataComponents.FLUID_CONTENT, stack, capacityMb) {

                @Override
                public boolean isFluidValid(int tank, FluidStack fluidStack) {
                    return fluidStack.getFluid() == GTMaterials.Oxygen.getFluid();
                }
            };
        }, item);

        event.registerItem(OxygenItemCap.OXYGEN_SUPPLY, (stack, ctx) -> {
            ensureConfigWritten(stack);
            return (IOxygenSupplyItem) this::drainTicks;
        }, item);
    }
}
