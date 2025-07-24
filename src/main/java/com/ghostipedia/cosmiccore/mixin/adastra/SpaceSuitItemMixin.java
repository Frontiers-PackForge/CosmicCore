package com.ghostipedia.cosmiccore.mixin.adastra;

import com.ghostipedia.cosmiccore.api.item.armor.SpaceArmorComponentItem;

import net.minecraft.world.item.ItemStack;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import earth.terrarium.adastra.common.items.armor.SpaceSuitItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = SpaceSuitItem.class, remap = false)
public abstract class SpaceSuitItemMixin {

    @ModifyReturnValue(method = "getOxygenAmount", at = @At(value = "RETURN", ordinal = 1))
    private static long cosmicCore$getSpaceArmorOxygen(long original, @Local ItemStack stack) {
        if (stack.getItem() instanceof SpaceArmorComponentItem suit) {
            return suit.getFluidContainer(stack).getFirstFluid().getFluidAmount();
        }
        return original;
    }
}
