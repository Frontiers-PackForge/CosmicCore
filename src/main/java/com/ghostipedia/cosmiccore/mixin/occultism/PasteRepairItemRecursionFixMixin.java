package com.ghostipedia.cosmiccore.mixin.occultism;

import com.gregtechceu.gtceu.api.item.IGTTool;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets = "com.klikli_dev.occultism.crafting.recipe.PasteRepairItemRecipe", remap = false)
public class PasteRepairItemRecursionFixMixin {

    @Unique
    private static final ThreadLocal<Boolean> COSMICCORE$INSIDE = ThreadLocal.withInitial(() -> Boolean.FALSE);

    @WrapMethod(method = "getRemainingItems(Lnet/minecraft/world/item/crafting/CraftingInput;)Lnet/minecraft/core/NonNullList;")
    private NonNullList<ItemStack> cosmiccore$breakRepairRecursion(CraftingInput input,
                                                                   Operation<NonNullList<ItemStack>> original) {
        if (COSMICCORE$INSIDE.get()) {
            NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
            for (int i = 0; i < remaining.size(); i++) {
                ItemStack stack = input.getItem(i);
                if (stack.hasCraftingRemainingItem()) {
                    ItemStack leftover = stack.getCraftingRemainingItem();
                    if (!(leftover.getItem() instanceof IGTTool)) {
                        remaining.set(i, leftover);
                    }
                }
            }
            return remaining;
        }
        COSMICCORE$INSIDE.set(Boolean.TRUE);
        try {
            return original.call(input);
        } finally {
            COSMICCORE$INSIDE.set(Boolean.FALSE);
        }
    }
}
