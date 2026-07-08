package com.ghostipedia.cosmiccore.mixin.gtfix;

import net.minecraft.world.item.ItemStack;

import brachy.modularui.screen.ModularContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ModularContainerMenu.class, remap = false)
public class ModularQuickMoveDupeFixMixin {

    @Redirect(method = "quickMoveStack",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"),
              remap = false)
    private boolean cosmiccore$bailOnlyWhenNothingMoved(ItemStack remainder, ItemStack attempted) {
        return ItemStack.matches(remainder, attempted);
    }
}
