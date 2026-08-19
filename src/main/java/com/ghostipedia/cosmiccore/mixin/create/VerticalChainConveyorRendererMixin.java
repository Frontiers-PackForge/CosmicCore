package com.ghostipedia.cosmiccore.mixin.create;

import com.ghostipedia.cosmiccore.common.compat.create.VerticalChainGeometry;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ChainConveyorRenderer.class, remap = false)
public abstract class VerticalChainConveyorRendererMixin {

    @Redirect(
              method = "renderBox",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F", ordinal = 0))
    private float cosmiccore$clampVerticalPackageZRotation(
                                                           float value, float minimum, float maximum,
                                                           @Local(ordinal = 2) Vec3 offset) {
        return VerticalChainGeometry.isSteepVisual(offset) ? 0 : Mth.clamp(value, minimum, maximum);
    }

    @Redirect(
              method = "renderBox",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F", ordinal = 1))
    private float cosmiccore$clampVerticalPackageXRotation(
                                                           float value, float minimum, float maximum,
                                                           @Local(ordinal = 2) Vec3 offset) {
        return VerticalChainGeometry.isSteepVisual(offset) ? 0 : Mth.clamp(value, minimum, maximum);
    }
}
