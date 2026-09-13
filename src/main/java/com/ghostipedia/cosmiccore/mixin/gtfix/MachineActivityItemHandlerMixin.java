package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.api.machine.activity.ActivityScope;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ItemStackHandler.class, remap = false)
public abstract class MachineActivityItemHandlerMixin {

    @WrapMethod(method = "extractItem")
    private ItemStack cosmiccore$extract(int slot, int amount, boolean simulate, Operation<ItemStack> original) {
        ActivityScope scope = ActivityScope.current();
        if (!ActivityScope.active() || simulate) return original.call(slot, amount, simulate);
        boolean root = scope.enterMutation();
        try {
            ItemStack result = original.call(slot, amount, false);
            if (root) ActivityScope.item(result, result.getCount(), true);
            return result;
        } catch (RuntimeException exception) {
            ActivityScope.partial("item", true);
            throw exception;
        } finally {
            scope.leaveMutation();
        }
    }

    @WrapMethod(method = "insertItem")
    private ItemStack cosmiccore$insert(int slot, ItemStack stack, boolean simulate, Operation<ItemStack> original) {
        ActivityScope scope = ActivityScope.current();
        if (!ActivityScope.active() || simulate) return original.call(slot, stack, simulate);
        boolean root = scope.enterMutation();
        int requested = stack.getCount();
        try {
            ItemStack result = original.call(slot, stack, false);
            if (root) ActivityScope.item(stack, Math.max(0, requested - result.getCount()), false);
            return result;
        } catch (RuntimeException exception) {
            ActivityScope.partial("item", false);
            throw exception;
        } finally {
            scope.leaveMutation();
        }
    }

    @WrapMethod(method = "setStackInSlot")
    private void cosmiccore$set(int slot, ItemStack stack, Operation<Void> original) {
        ActivityScope scope = ActivityScope.current();
        if (!ActivityScope.active()) {
            original.call(slot, stack);
            return;
        }
        boolean root = scope.enterMutation();
        ItemStackHandler handler = (ItemStackHandler) (Object) this;
        ItemStack before = root ? handler.getStackInSlot(slot).copy() : ItemStack.EMPTY;
        try {
            original.call(slot, stack);
        } finally {
            if (root) {
                ItemStack after = handler.getStackInSlot(slot);
                if (ItemStack.isSameItemSameComponents(before, after)) {
                    int change = after.getCount() - before.getCount();
                    ActivityScope.item(change < 0 ? before : after, Math.abs((long) change), change < 0);
                } else {
                    ActivityScope.item(before, before.getCount(), true);
                    ActivityScope.item(after, after.getCount(), false);
                }
            }
            scope.leaveMutation();
        }
    }
}
