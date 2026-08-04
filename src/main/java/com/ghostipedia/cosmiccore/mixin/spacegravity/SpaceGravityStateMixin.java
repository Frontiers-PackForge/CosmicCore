package com.ghostipedia.cosmiccore.mixin.spacegravity;

import com.ghostipedia.cosmiccore.common.firmament.FirmamentFreeDriftSteering;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.spacegravity.spacegravity.SpaceGravityState;
import com.spacegravity.spacegravity.ZeroGravityInputState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = SpaceGravityState.class, remap = false)
public final class SpaceGravityStateMixin {

    @WrapOperation(
                   method = "handleClientInput",
                   at = @At(
                            value = "INVOKE",
                            target = "Lcom/spacegravity/spacegravity/ZeroGravityPhysics;computeFreeThrustVelocityDelta(Lcom/spacegravity/spacegravity/ZeroGravityInputState;)Lnet/minecraft/world/phys/Vec3;"))
    private static Vec3 cosmiccore$shapeManagedThrust(ZeroGravityInputState input, Operation<Vec3> original,
                                                      ServerPlayer player, ZeroGravityInputState packetInput) {
        return FirmamentFreeDriftSteering.shapeThrust(player, packetInput, original.call(input));
    }

    @Redirect(
              method = "handleClientInput",
              at = @At(
                       value = "FIELD",
                       target = "Lnet/minecraft/server/level/ServerPlayer;hurtMarked:Z",
                       opcode = 181))
    private static void cosmiccore$avoidManagedVelocityCorrection(ServerPlayer player, boolean value) {
        if (!FirmamentFreeDriftSteering.isManaged(player)) player.hurtMarked = value;
    }
}
