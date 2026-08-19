package com.ghostipedia.cosmiccore.mixin.create;

import com.ghostipedia.cosmiccore.common.compat.create.VerticalChainGeometry;

import net.minecraft.world.phys.Vec3;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorConnectionHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ChainConveyorConnectionHandler.class, remap = false)
public abstract class VerticalChainConveyorConnectionHandlerMixin {

    @ModifyExpressionValue(
                           method = "validateAndConnect",
                           at = @At(
                                    value = "INVOKE",
                                    target = "Lnet/minecraft/world/phys/Vec3;multiply(DDD)Lnet/minecraft/world/phys/Vec3;"))
    private static Vec3 cosmiccore$allowVerticalConnections(Vec3 horizontalProjection) {
        return VerticalChainGeometry.unrestrictedHorizontalProjection(horizontalProjection);
    }

    @Redirect(
              method = "clientTick",
              at = @At(
                       value = "INVOKE",
                       target = "Lnet/minecraft/world/phys/Vec3;cross(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"))
    private static Vec3 cosmiccore$stabilizeVerticalOutline(Vec3 direction, Vec3 axis) {
        return VerticalChainGeometry.stableCross(direction, axis);
    }
}
