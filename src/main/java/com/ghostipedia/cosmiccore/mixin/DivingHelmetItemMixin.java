package com.ghostipedia.cosmiccore.mixin;

import com.ghostipedia.cosmiccore.common.breath.OxygenHelper;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;

import com.simibubi.create.content.equipment.armor.DivingHelmetItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = DivingHelmetItem.class, remap = false)
public class DivingHelmetItemMixin {

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
