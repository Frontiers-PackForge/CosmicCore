package com.ghostipedia.cosmiccore.mixin;

import com.ghostipedia.cosmiccore.common.breath.OxygenHelper;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fluids.FluidType;

import com.simibubi.create.content.equipment.armor.DivingHelmetItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = DivingHelmetItem.class, remap = false)

public class DivingHelmetItemMixin {

    /**
     * Activate helmet "if in water or lava" -> "if in water or bad air or lava"
     */
    @Redirect(method = "breatheUnderwater(Lnet/minecraftforge/event/entity/living/LivingEvent$LivingTickEvent;)V",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/entity/LivingEntity;canDrownInFluidType(Lnet/minecraftforge/fluids/FluidType;)Z"))
    private static boolean redirectCanDrownInFluidType(LivingEntity entity, FluidType fluidtype) {
        return entity.isInFluidType() ||
                (fluidtype == (entity.getEyeInFluidType()) && OxygenHelper.airQualityActivatesHelmet(entity));
    }
}
