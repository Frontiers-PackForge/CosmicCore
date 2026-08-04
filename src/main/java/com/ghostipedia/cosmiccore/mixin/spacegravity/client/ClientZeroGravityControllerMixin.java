package com.ghostipedia.cosmiccore.mixin.spacegravity.client;

import com.ghostipedia.cosmiccore.client.gravity.FreeDriftPresentationAngles;
import com.ghostipedia.cosmiccore.common.firmament.FirmamentFreeDriftSteering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.spacegravity.spacegravity.ClientZeroGravityController;
import com.spacegravity.spacegravity.ZeroGravityInputState;
import com.spacegravity.spacegravity.ZeroGravityOrientation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ClientZeroGravityController.class, remap = false)
public final class ClientZeroGravityControllerMixin {

    @WrapOperation(
                   method = "onMovementInput",
                   at = @At(
                            value = "INVOKE",
                            target = "Lcom/spacegravity/spacegravity/ZeroGravityPhysics;computeFreeThrustVelocityDelta(Lcom/spacegravity/spacegravity/ZeroGravityInputState;)Lnet/minecraft/world/phys/Vec3;"))
    private static Vec3 cosmiccore$shapeManagedThrust(ZeroGravityInputState input, Operation<Vec3> original) {
        Vec3 thrustDelta = original.call(input);
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? thrustDelta : FirmamentFreeDriftSteering.shapeThrust(player, input, thrustDelta);
    }

    @ModifyReturnValue(method = "getPushAnimationProgress", at = @At("RETURN"))
    private static float cosmiccore$suppressSyntheticPushAnimation(float original, Player player) {
        return FirmamentFreeDriftSteering.isManaged(player) ? 0.0f : original;
    }

    @ModifyExpressionValue(
                           method = "applyOrientationToPlayer",
                           at = @At(
                                    value = "INVOKE",
                                    target = "Lcom/spacegravity/spacegravity/ZeroGravityOrientation;toCameraAngles(Lcom/spacegravity/spacegravity/ZeroGravityOrientation$OrientationData;)Lcom/spacegravity/spacegravity/ZeroGravityOrientation$CameraAngles;"))
    private static ZeroGravityOrientation.CameraAngles cosmiccore$stabilizePlayerAngles(
                                                                                        ZeroGravityOrientation.CameraAngles original,
                                                                                        @Local(argsOnly = true) Player player,
                                                                                        @Local(argsOnly = true) ZeroGravityOrientation.OrientationData orientation) {
        return FreeDriftPresentationAngles.stabilize(player, orientation, original);
    }

    @ModifyExpressionValue(
                           method = "onCameraAngles",
                           at = @At(
                                    value = "INVOKE",
                                    target = "Lcom/spacegravity/spacegravity/ZeroGravityOrientation;toCameraAngles(Lcom/spacegravity/spacegravity/ZeroGravityOrientation$OrientationData;)Lcom/spacegravity/spacegravity/ZeroGravityOrientation$CameraAngles;"))
    private static ZeroGravityOrientation.CameraAngles cosmiccore$stabilizeCameraAngles(
                                                                                        ZeroGravityOrientation.CameraAngles original) {
        LocalPlayer player = Minecraft.getInstance().player;
        ZeroGravityOrientation.OrientationData orientation = player == null ? null :
                ClientZeroGravityController.getOrientationForVisual(player);
        return player == null || orientation == null ? original :
                FreeDriftPresentationAngles.stabilize(player, orientation, original);
    }

    @ModifyExpressionValue(
                           method = "onRenderPlayerPre",
                           at = @At(
                                    value = "INVOKE",
                                    target = "Lcom/spacegravity/spacegravity/ZeroGravityOrientation;toCameraAngles(Lcom/spacegravity/spacegravity/ZeroGravityOrientation$OrientationData;)Lcom/spacegravity/spacegravity/ZeroGravityOrientation$CameraAngles;"))
    private static ZeroGravityOrientation.CameraAngles cosmiccore$stabilizeModelAngles(
                                                                                       ZeroGravityOrientation.CameraAngles original,
                                                                                       @Local(argsOnly = true) RenderPlayerEvent.Pre event) {
        ZeroGravityOrientation.OrientationData orientation = ClientZeroGravityController
                .getOrientationForVisual(event.getEntity());
        return orientation == null ? original :
                FreeDriftPresentationAngles.stabilize(event.getEntity(), orientation, original);
    }
}
