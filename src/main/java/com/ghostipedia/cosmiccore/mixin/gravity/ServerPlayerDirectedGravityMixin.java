package com.ghostipedia.cosmiccore.mixin.gravity;

import com.ghostipedia.cosmiccore.common.gravity.DirectedGravityCollision;
import com.ghostipedia.cosmiccore.common.gravity.DirectedGravityKernel;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerDirectedGravityMixin {

    @Unique
    private Vec3 cosmiccore$packetFallMovement = Vec3.ZERO;

    @Inject(method = "doCheckFallDamage(DDDZ)V", at = @At("HEAD"))
    private void cosmiccore$capturePacketFallMovement(double x, double y, double z, boolean onGround,
                                                      CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (DirectedGravityKernel.isActive(player)) cosmiccore$packetFallMovement = new Vec3(x, y, z);
    }

    @WrapOperation(
                   method = "doCheckFallDamage(DDDZ)V",
                   at = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/entity/player/Player;checkFallDamage(DZLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V"))
    private void cosmiccore$checkPacketFallInLocalFrame(ServerPlayer instance, double y, boolean onGround,
                                                        BlockState state, BlockPos pos, Operation<Void> original) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (DirectedGravityKernel.isActive(player)) {
            original.call(instance, DirectedGravityCollision.localVertical(player, cosmiccore$packetFallMovement),
                    player.onGround(), state, pos);
        } else {
            original.call(instance, y, onGround, state, pos);
        }
    }
}
