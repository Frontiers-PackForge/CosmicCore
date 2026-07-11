package com.ghostipedia.cosmiccore.mixin.undergarden;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quek.undergarden.event.UthericInfectionEvents;

@Mixin(value = UthericInfectionEvents.class, remap = false)
public class UthericInfectionDisableMixin {

    @Inject(method = "tickUthericInfection", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cosmiccore$disableInfectionTick(EntityTickEvent.Pre event, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "onEntityHit", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cosmiccore$disableInfectionSpread(LivingIncomingDamageEvent event, CallbackInfo ci) {
        ci.cancel();
    }
}
