package com.ghostipedia.cosmiccore.mixin.gravity;

import com.ghostipedia.cosmiccore.common.gravity.DirectedGravityKernel;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerDirectedGravityMixin {

    @Shadow
    public ServerPlayer player;

    @Shadow
    private double lastGoodX;

    @Shadow
    private double lastGoodY;

    @Shadow
    private double lastGoodZ;

    @Shadow
    private boolean clientIsFloating;

    @Shadow
    private int aboveGroundTickCount;

    @Shadow
    private static double clampHorizontal(double value) {
        throw new AssertionError();
    }

    @Shadow
    private static double clampVertical(double value) {
        throw new AssertionError();
    }

    @Unique
    private Vec3 cosmiccore$packetMovement = Vec3.ZERO;

    @Inject(
            method = "handleMovePlayer(Lnet/minecraft/network/protocol/game/ServerboundMovePlayerPacket;)V",
            at = @At(
                     value = "INVOKE",
                     target = "Lnet/minecraft/server/level/ServerPlayer;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
                     shift = At.Shift.BEFORE))
    private void cosmiccore$handleLocalUpBeforeMove(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        if (!DirectedGravityKernel.isActive(player)) return;
        cosmiccore$packetMovement = new Vec3(
                clampHorizontal(packet.getX(player.getX())) - lastGoodX,
                clampVertical(packet.getY(player.getY())) - lastGoodY,
                clampHorizontal(packet.getZ(player.getZ())) - lastGoodZ);
        double localVertical = DirectedGravityKernel.worldToLocal(player, cosmiccore$packetMovement).y;
        if (localVertical > 0.0) player.resetFallDistance();
        if (player.onGround() && !packet.isOnGround() && localVertical > 0.0) {
            player.jumpFromGround();
        }
    }

    @WrapOperation(
                   method = "handleMovePlayer(Lnet/minecraft/network/protocol/game/ServerboundMovePlayerPacket;)V",
                   at = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/server/level/ServerPlayer;jumpFromGround()V"))
    private void cosmiccore$suppressWorldUpJump(ServerPlayer instance, Operation<Void> original) {
        if (!DirectedGravityKernel.isActive(player)) original.call(instance);
    }

    @WrapOperation(
                   method = "handleMovePlayer(Lnet/minecraft/network/protocol/game/ServerboundMovePlayerPacket;)V",
                   at = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/server/level/ServerPlayer;setOnGroundWithMovement(ZLnet/minecraft/world/phys/Vec3;)V"))
    private void cosmiccore$validateLocalSupport(ServerPlayer instance, boolean packetOnGround, Vec3 movement,
                                                 Operation<Void> original) {
        original.call(instance, DirectedGravityKernel.isActive(player) ? player.verticalCollisionBelow : packetOnGround,
                movement);
    }

    @WrapOperation(
                   method = "handleMovePlayer(Lnet/minecraft/network/protocol/game/ServerboundMovePlayerPacket;)V",
                   at = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/server/level/ServerPlayer;resetFallDistance()V"))
    private void cosmiccore$resetFallDistanceForLocalUp(ServerPlayer instance, Operation<Void> original) {
        if (!DirectedGravityKernel.isActive(player) ||
                DirectedGravityKernel.worldToLocal(player, cosmiccore$packetMovement).y > 0.0) {
            original.call(instance);
        }
    }

    @Inject(
            method = "handleMovePlayer(Lnet/minecraft/network/protocol/game/ServerboundMovePlayerPacket;)V",
            at = @At("RETURN"))
    private void cosmiccore$clearWorldFloatingValidation(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        if (DirectedGravityKernel.isActive(player)) {
            clientIsFloating = false;
            aboveGroundTickCount = 0;
        }
    }
}
