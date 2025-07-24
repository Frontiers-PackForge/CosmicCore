package com.ghostipedia.cosmiccore.mixin.create;

import com.ghostipedia.cosmiccore.common.breath.OxygenHelper;

import net.minecraft.world.entity.LivingEntity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.equipment.armor.DivingHelmetItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = DivingHelmetItem.class, remap = false)
public class DivingHelmetItemMixin {

    /**
     * Activate helmet "if in water or lava" -> "if in water or bad air or lava"
     */
    @ModifyExpressionValue(method = "breatheUnderwater(Lnet/minecraftforge/event/entity/living/LivingEvent$LivingTickEvent;)V",
                           at = @At(value = "INVOKE",
                                    target = "Lnet/minecraft/world/entity/LivingEntity;canDrownInFluidType(Lnet/minecraftforge/fluids/FluidType;)Z"))
    private static boolean cosmicCore$drownInAir(boolean canDrown, @Local LivingEntity entity) {
        return canDrown || OxygenHelper.airQualityActivatesHelmet(entity);
    }
}
