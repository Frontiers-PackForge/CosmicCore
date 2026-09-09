package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.common.compat.gtceu.RainSealable;

import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.common.machine.trait.EnvironmentalExplosionTrait;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = EnvironmentalExplosionTrait.class, remap = false)
public abstract class RainSealEnvironmentMixin extends MachineTrait {

    @Redirect(method = "checkEnvironment",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/level/Level;isRainingAt(Lnet/minecraft/core/BlockPos;)Z",
                       remap = true))
    private boolean cosmiccore$sealedFromRain(Level level, BlockPos pos) {
        return !((RainSealable) getMachine()).cosmiccore$isRainSealed() && level.isRainingAt(pos);
    }

    @Redirect(method = "checkEnvironment",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/level/material/FluidState;isEmpty()Z",
                       remap = true))
    private boolean cosmiccore$sealedFromWater(FluidState fluid) {
        return fluid.isEmpty() || (fluid.is(FluidTags.WATER) &&
                ((RainSealable) getMachine()).cosmiccore$isRainSealed());
    }
}
