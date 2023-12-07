package com.ghostipedia.CosmicCore.mixin;

import com.ghostipedia.CosmicCore.CosmicCore;
import com.ghostipedia.CosmicCore.common.breath.OxygenHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import com.simibubi.create.content.equipment.armor.DivingHelmetItem;
@Mixin(value = DivingHelmetItem.class, remap = false)

public class DivingHelmetItemMixin {
    /**
     * Activate helmet "if in water or lava" -> "if in water or bad air or lava"
     */
    @Redirect(method = "breatheUnderwater(Lnet/minecraftforge/event/entity/living/LivingEvent$LivingTickEvent;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;canDrownInFluidType(Lnet/minecraftforge/fluids/FluidType;)Z"))
    private boolean redirectCanDrownInFluidType(LivingEntity entity, FluidType fluidtype) {
        final var res = entity.isInFluidType();
        if (fluidtype == (entity.getEyeInFluidType()))
        {
            return res || OxygenHelper.airQualityActivatesHelmet(entity);
        }
        return res;
    }
}
