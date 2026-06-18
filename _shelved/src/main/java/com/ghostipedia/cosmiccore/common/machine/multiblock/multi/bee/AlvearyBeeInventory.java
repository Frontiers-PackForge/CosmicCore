package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.bee;

import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;

import net.minecraft.world.item.ItemStack;

import forestry.api.apiculture.IBeeHousingInventory;

/**
 * Wraps a thread's colored input/output bus as IBeeHousingInventory.
 * Input bus: slot 0 = queen, slot 1 = drone.
 * Output bus: addProduct deposits via insertItemInternal to bypass IO filtering.
 */
public class AlvearyBeeInventory implements IBeeHousingInventory {

    private final NotifiableItemStackHandler inputHandler;
    private final NotifiableItemStackHandler outputHandler;

    public AlvearyBeeInventory(IRecipeHandler<?> inputRecipeHandler, IRecipeHandler<?> outputRecipeHandler) {
        this.inputHandler = extractHandler(inputRecipeHandler);
        this.outputHandler = extractHandler(outputRecipeHandler);
    }

    private static NotifiableItemStackHandler extractHandler(IRecipeHandler<?> handler) {
        if (handler instanceof NotifiableItemStackHandler notifiable) {
            return notifiable;
        }
        throw new IllegalArgumentException(
                "Recipe handler is not NotifiableItemStackHandler: " + handler.getClass().getName());
    }

    @Override
    public ItemStack getQueen() {
        return inputHandler.getStackInSlot(0);
    }

    @Override
    public ItemStack getDrone() {
        return inputHandler.getStackInSlot(1);
    }

    @Override
    public void setQueen(ItemStack stack) {
        inputHandler.setStackInSlot(0, stack);
    }

    @Override
    public void setDrone(ItemStack stack) {
        inputHandler.setStackInSlot(1, stack);
    }

    @Override
    public boolean addProduct(ItemStack product, boolean all) {
        if (product.isEmpty()) return true;

        ItemStack remaining = product.copy();
        for (int i = 0; i < outputHandler.getSlots(); i++) {
            remaining = outputHandler.insertItemInternal(i, remaining, false);
            if (remaining.isEmpty()) return true;
        }
        return false;
    }
}
