package com.ghostipedia.cosmiccore.mixin.undergarden;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quek.undergarden.event.UthericInfectionEvents;
import quek.undergarden.registry.UGAttachments;

@Mixin(value = UthericInfectionEvents.class, remap = false)
public class UthericInfectionDisableMixin {

    @Inject(method = "tickUthericInfection", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cosmiccore$disableInfectionTick(EntityTickEvent.Pre event, CallbackInfo ci) {
        Entity entity = event.getEntity();
        if (!entity.level().isClientSide() && entity instanceof LivingEntity living &&
                cosmiccore$clearInfection(living)) {
            UthericInfectionEvents.sendInfectionSyncPacket(living);
        }
        ci.cancel();
    }

    @Inject(method = "onEntityHit", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cosmiccore$disableInfectionSpread(LivingIncomingDamageEvent event, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "sendInfectionSyncPacket", at = @At("HEAD"), remap = false)
    private static void cosmiccore$clearInfectionBeforeSync(Entity entity, CallbackInfo ci) {
        if (entity instanceof LivingEntity living) {
            cosmiccore$clearInfection(living);
        }
    }

    private static boolean cosmiccore$clearInfection(LivingEntity living) {
        double infection = living.getData(UGAttachments.UTHERIC_INFECTION);
        float previousDamage = living.getData(UGAttachments.PREVIOUS_UTHERIC_INFECTION_DAMAGE);
        if (infection == 0.0 && previousDamage == 0.0f) {
            return false;
        }
        living.setData(UGAttachments.UTHERIC_INFECTION, 0.0);
        living.setData(UGAttachments.PREVIOUS_UTHERIC_INFECTION_DAMAGE, 0.0f);
        return true;
    }
}
