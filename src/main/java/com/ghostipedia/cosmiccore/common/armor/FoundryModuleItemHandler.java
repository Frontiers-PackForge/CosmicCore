package com.ghostipedia.cosmiccore.common.armor;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.module.FluidStorageModule;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public final class FoundryModuleItemHandler extends CustomItemStackHandler {

    private final CustomItemStackHandler original;
    private final Supplier<ItemStack> equipment;

    public FoundryModuleItemHandler(CustomItemStackHandler original, Supplier<ItemStack> equipment) {
        super(original.getStacks());
        this.original = original;
        this.equipment = equipment;
        setOnContentsChanged(original.getOnContentsChanged());
    }

    private void refresh(int slot) {
        var modular = GTCapabilityHelper.getModularItem(equipment.get());
        if (modular == null) return;
        var context = modular.getModuleContextForSlot(slot);
        if (context != null && (context.getModule() instanceof OxygenTankModule ||
                context.getModule() instanceof FluidStorageModule)) {
            setStackInSlotNoCallback(slot, context.getData().getModuleItem().copy());
        }
    }

    private void refreshAll() {
        for (int slot = 0; slot < getSlots(); slot++) refresh(slot);
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        refresh(slot);
        return super.getStackInSlot(slot);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        refresh(slot);
        return super.extractItem(slot, amount, simulate);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        refresh(slot);
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return original.isItemValid(slot, stack);
    }

    @Override
    public int getSlotLimit(int slot) {
        return original.getSlotLimit(slot);
    }

    @Override
    public void onContentsChanged(int slot) {
        original.onContentsChanged(slot);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        refreshAll();
        return super.serializeNBT(provider);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        original.deserializeNBT(provider, tag);
        stacks = original.getStacks();
    }

    @Override
    public NonNullList<ItemStack> getStacks() {
        refreshAll();
        return super.getStacks();
    }

    @Override
    public void dropInventoryInWorld(Level level, BlockPos pos) {
        refreshAll();
        super.dropInventoryInWorld(level, pos);
    }
}
