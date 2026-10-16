package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.common.module.AirSupplierModule;
import com.gregtechceu.gtceu.common.module.FluidStorageModule;

import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemModule.class, remap = false)
public abstract class LegacyArmorModuleInstallMixin {

    @Inject(method = "canApplyTo", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$blockLegacyInstallation(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Object module = this;
        if (module instanceof AirSupplierModule || module instanceof FluidStorageModule) cir.setReturnValue(false);
    }
}
