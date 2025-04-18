package com.ghostipedia.cosmiccore.mixin.tinkers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.item.ItemStack;

import slimeknights.tconstruct.library.tools.item.armor.ModifiableArmorItem;

@Mixin(value = ModifiableArmorItem.class, remap = false)
public class ModifiableArmorItemMixin {
    @Inject(method = "isEnchantable", at = @At("RETURN"), cancellable = true)
    public void isEnchantable(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
    @Inject(method = "isBookEnchantable", at = @At("RETURN"), cancellable = true)
    public void isBookEnchantable(ItemStack stack, ItemStack book, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
