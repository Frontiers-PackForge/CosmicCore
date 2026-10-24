package com.ghostipedia.cosmiccore.common.armor;

import com.ghostipedia.cosmiccore.common.item.behavior.OxygenSupplyTankBehavior;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.armor.ModularArmorItem;
import com.gregtechceu.gtceu.api.item.module.CapabilityProviderItemModule;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.common.module.FluidStorageModule;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

public class OxygenTankModule extends CapabilityProviderItemModule<IFluidHandlerItem> {

    private final OxygenSupplyTankBehavior tank;

    public OxygenTankModule(ResourceLocation id, int capacity, int efficiency) {
        this(id, new OxygenSupplyTankBehavior(capacity, efficiency));
    }

    OxygenTankModule(ResourceLocation id, OxygenSupplyTankBehavior tank) {
        super(id);
        this.tank = tank;
    }

    public OxygenSupplyTankBehavior tank() {
        return tank;
    }

    @Override
    public ItemStack copyCapabilityDataToModuleItem(IFluidHandlerItem cap, ModuleContext context) {
        var current = context.getModularItemStack().getModuleContextForSlot(context.getData().getSlot());
        return current != null && current.getModule() == this ? current.getData().getModuleItem().copy() :
                context.getData().getModuleItem().copy();
    }

    @Override
    public boolean canApplyTo(ItemStack stack) {
        if (!(stack.getItem() instanceof ModularArmorItem armor) ||
                armor.getEquipmentSlot() != EquipmentSlot.CHEST)
            return false;
        var modular = GTCapabilityHelper.getModularItem(stack);
        return modular != null &&
                modular.getAllModuleInstances().stream()
                        .noneMatch(value -> value.getModule() instanceof OxygenTankModule ||
                                value.getModule() instanceof FluidStorageModule);
    }

    @Override
    public ItemCapability<IFluidHandlerItem, Void> getCapability() {
        return Capabilities.FluidHandler.ITEM;
    }

    @Override
    public Component getInfo() {
        return Component.translatable(getDescriptionLanguageKey());
    }

    @Override
    public void applyInitialCapabilityData(IFluidHandlerItem cap, ModuleContext context) {}

    @Override
    public void clearCapabilityFromStack(ModuleContext context, ItemStack stack) {}

    @Override
    public IFluidHandlerItem createCapabilityForStack(ModuleContext context, ItemStack stack) {
        return new IFluidHandlerItem() {

            private ModuleContext current() {
                return context.getModularItemStack().getModuleContextForSlot(context.getData().getSlot());
            }

            private ItemStack contents() {
                var now = current();
                return now != null && now.getModule() == OxygenTankModule.this ? now.getData().getModuleItem() :
                        ItemStack.EMPTY;
            }

            private IFluidHandlerItem handler(ItemStack item) {
                return item.getCapability(Capabilities.FluidHandler.ITEM);
            }

            private void save(ItemStack item) {
                var now = current();
                if (now != null && now.getModule() == OxygenTankModule.this)
                    now.setData(now.getData().withModuleItem(item));
            }

            @Override
            public ItemStack getContainer() {
                return stack;
            }

            @Override
            public int getTanks() {
                return 1;
            }

            @Override
            public FluidStack getFluidInTank(int index) {
                var item = contents();
                return item.isEmpty() ? FluidStack.EMPTY : handler(item).getFluidInTank(index);
            }

            @Override
            public int getTankCapacity(int index) {
                return tank.getCapacityMb();
            }

            @Override
            public boolean isFluidValid(int index, FluidStack fluid) {
                return tank.acceptsFluid(fluid);
            }

            @Override
            public int fill(FluidStack fluid, FluidAction action) {
                var item = contents().copy();
                if (item.isEmpty()) return 0;
                int amount = handler(item).fill(fluid, action);
                if (action.execute() && amount > 0) save(item);
                return amount;
            }

            @Override
            public FluidStack drain(FluidStack fluid, FluidAction action) {
                var item = contents().copy();
                if (item.isEmpty()) return FluidStack.EMPTY;
                var drained = handler(item).drain(fluid, action);
                if (action.execute() && !drained.isEmpty()) save(item);
                return drained;
            }

            @Override
            public FluidStack drain(int amount, FluidAction action) {
                var item = contents().copy();
                if (item.isEmpty()) return FluidStack.EMPTY;
                var drained = handler(item).drain(amount, action);
                if (action.execute() && !drained.isEmpty()) save(item);
                return drained;
            }
        };
    }
}
