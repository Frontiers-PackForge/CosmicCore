package com.ghostipedia.cosmiccore.common.item.behavior;

import com.ghostipedia.cosmiccore.common.airControl.IOxygenSupplyItem;
import com.ghostipedia.cosmiccore.common.airControl.OxygenItemCap;
import com.ghostipedia.cosmiccore.utils.ItemData;

import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IComponentCapability;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;

import lombok.Getter;

public class OxygenSupplyTankBehavior implements IItemComponent, IComponentCapability {

    private static final String TAG_ROOT = "CosmicCoreO2";
    private static final String TAG_BUF = "TickBuffer";
    private static final String TAG_TPM = "TicksPerMb";

    @Getter
    private final int capacityMb;
    @Getter
    private final int ticksPerMb;

    public OxygenSupplyTankBehavior(int capacityMb, int ticksPerMb) {
        this.capacityMb = capacityMb;
        this.ticksPerMb = Math.max(1, ticksPerMb);
    }

    protected Fluid oxygenFluid() {
        return GTMaterials.Oxygen.getFluid();
    }

    public boolean acceptsFluid(FluidStack fluid) {
        return fluid.getFluid() == oxygenFluid();
    }

    public int drainTicks(ItemStack stack, int requestTicks) {
        if (requestTicks <= 0) return 0;

        IFluidHandlerItem fluidHandler = getFluidHandler(stack);
        if (fluidHandler == null) return 0;

        int buffer = getTickBuffer(stack);
        if (buffer < requestTicks) {
            int neededMb = (requestTicks - buffer + ticksPerMb - 1) / ticksPerMb;
            FluidStack drained = fluidHandler.drain(
                    new FluidStack(oxygenFluid(), neededMb),
                    IFluidHandlerItem.FluidAction.EXECUTE);
            if (!drained.isEmpty()) {
                buffer += drained.getAmount() * ticksPerMb;
            }
        }

        int provided = Math.min(requestTicks, buffer);
        if (provided > 0) {
            setTickBuffer(stack, buffer - provided);
        }
        return provided;
    }

    public static long remainingTicks(ItemStack stack) {
        IFluidHandlerItem fluidHandler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (fluidHandler == null) return 0;
        CompoundTag tag = ItemData.readElement(stack, TAG_ROOT);
        int ticksPerMb = 0;
        if (stack.getItem() instanceof IComponentItem item) {
            for (var component : item.getComponents()) {
                if (component instanceof OxygenSupplyTankBehavior tank) ticksPerMb = tank.getTicksPerMb();
            }
        }
        if (ticksPerMb <= 0) return 0;
        return (long) fluidHandler.getFluidInTank(0).getAmount() * ticksPerMb + tag.getInt(TAG_BUF);
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

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event, Item item) {
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> {
            return new FluidHandlerItemStack(GTDataComponents.FLUID_CONTENT, stack, capacityMb) {

                @Override
                public boolean isFluidValid(int tank, FluidStack fluidStack) {
                    return acceptsFluid(fluidStack);
                }
            };
        }, item);

        event.registerItem(OxygenItemCap.OXYGEN_SUPPLY, (stack, ctx) -> {
            return (IOxygenSupplyItem) this::drainTicks;
        }, item);
    }
}
