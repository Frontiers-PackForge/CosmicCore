package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.common.module.AirSupplierModule;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AirSupplierModule.class, remap = false)
public abstract class LegacyArmorAirSupplierMixin {

    @Inject(method = "getInfo", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$legacyRemovalInfo(CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue(Component.translatable("cosmiccore.armor.legacy_air_supplier"));
    }

    @Inject(method = "onArmorTick", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$retireAirSupplier(ModuleContext context, LivingEntity entity, CallbackInfo ci) {
        ci.cancel();
    }
}
