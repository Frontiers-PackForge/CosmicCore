package com.ghostipedia.cosmiccore.mixin;

import com.ghostipedia.cosmiccore.common.armor.AbyssModule;
import com.ghostipedia.cosmiccore.common.armor.ArmorModuleHelper;
import com.ghostipedia.cosmiccore.common.armor.InstalledOxygen;
import com.ghostipedia.cosmiccore.common.breath.OxygenHelper;
import com.ghostipedia.cosmiccore.common.murkbloom.MurkbloomServerLogic;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;

import com.simibubi.create.content.equipment.armor.DivingHelmetItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DivingHelmetItem.class, remap = false)
public class DivingHelmetItemMixin {

    @Inject(method = "breatheUnderwater", at = @At("HEAD"), cancellable = true)
    private static void cosmiccore$modularOxygenOwner(LivingBreatheEvent event, CallbackInfo ci) {
        if (event.getEntity() instanceof Player player &&
                (MurkbloomServerLogic.inHollow(player.level(),
                        player.getY()) ||
                        InstalledOxygen.hasTank(player) ||
                        ArmorModuleHelper.tier(player,
                                EquipmentSlot.HEAD,
                                AbyssModule.Role.REBREATHER) > 0))
            ci.cancel();
    }

    /**
     * Activate helmet "if can't breathe or in lava" -> "if can't breathe or bad air or in lava"
     * Redirects the event.canBreathe() check to also consider bad air quality.
     */
    @Redirect(method = "breatheUnderwater",
              at = @At(value = "INVOKE",
                       target = "Lnet/neoforged/neoforge/event/entity/living/LivingBreatheEvent;canBreathe()Z"))
    private static boolean cosmicCore$redirectCanBreathe(LivingBreatheEvent event) {
        LivingEntity entity = event.getEntity();
        // Return false (can't breathe) if air quality is bad, so helmet activates
        if (OxygenHelper.airQualityActivatesHelmet(entity)) {
            return false;
        }
        return event.canBreathe();
    }
}
