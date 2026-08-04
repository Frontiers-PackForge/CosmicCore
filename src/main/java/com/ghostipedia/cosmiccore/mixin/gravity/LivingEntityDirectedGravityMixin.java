package com.ghostipedia.cosmiccore.mixin.gravity;

import com.ghostipedia.cosmiccore.common.gravity.DirectedGravityKernel;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDirectedGravityMixin {

    @Shadow
    protected abstract float getJumpPower();

    @Inject(method = "jumpFromGround()V", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$jumpAlongLocalUp(CallbackInfo ci) {
        LivingEntity living = (LivingEntity) (Object) this;
        if (!(living instanceof Player player) || !DirectedGravityKernel.isActive(player)) return;
        float power = getJumpPower();
        if (power <= 1.0E-5F) {
            ci.cancel();
            return;
        }
        Vec3 localVelocity = DirectedGravityKernel.worldToLocal(player, player.getDeltaMovement());
        player.setDeltaMovement(DirectedGravityKernel.localToWorld(
                player, new Vec3(localVelocity.x, power, localVelocity.z)));
        if (player.isSprinting()) {
            double yaw = player.getYRot() * Math.PI / 180.0;
            player.addDeltaMovement(DirectedGravityKernel.localToWorld(
                    player, new Vec3(-Math.sin(yaw) * 0.2, 0.0, Math.cos(yaw) * 0.2)));
        }
        player.hasImpulse = true;
        net.neoforged.neoforge.common.CommonHooks.onLivingJump(player);
        ci.cancel();
    }

    @Inject(method = "travel(Lnet/minecraft/world/phys/Vec3;)V", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$travelInLocalFrame(Vec3 travelVector, CallbackInfo ci) {
        LivingEntity living = (LivingEntity) (Object) this;
        if (!(living instanceof Player player) || !DirectedGravityKernel.isActive(player)) return;
        if (player.isControlledByLocalInstance()) {
            double gravity = player.getGravity() * DirectedGravityKernel.frame(player).strength();
            Vec3 startingLocal = DirectedGravityKernel.worldToLocal(player, player.getDeltaMovement());
            if (startingLocal.y <= 0.0 && player.hasEffect(MobEffects.SLOW_FALLING)) {
                gravity = Math.min(gravity, 0.01);
            }

            BlockPos onPos = player.getBlockPosBelowThatAffectsMyMovement();
            float friction = player.level().getBlockState(onPos).getFriction(player.level(), onPos, player);
            float drag = player.onGround() ? friction * 0.91F : 0.91F;
            Vec3 moved = player.handleRelativeFrictionAndCalculateMovement(travelVector, friction);
            Vec3 local = DirectedGravityKernel.worldToLocal(player, moved);
            double localY = local.y;
            if (player.hasEffect(MobEffects.LEVITATION)) {
                double target = 0.05 * (player.getEffect(MobEffects.LEVITATION).getAmplifier() + 1);
                localY += (target - localY) * 0.2;
            } else if (!player.level().isClientSide || player.level().hasChunkAt(onPos)) {
                localY -= gravity;
            } else {
                localY = -0.1;
            }

            if (player.shouldDiscardFriction()) {
                player.setDeltaMovement(DirectedGravityKernel.localToWorld(
                        player, new Vec3(local.x, localY, local.z)));
            } else {
                player.setDeltaMovement(DirectedGravityKernel.localToWorld(
                        player, new Vec3(local.x * drag, localY * 0.98F, local.z * drag)));
            }
        }
        player.calculateEntityAnimation(false);
        ci.cancel();
    }

    @Inject(method = "wouldNotSuffocateAtTargetPose", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$checkDirectionalTargetPose(Pose pose, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity living = (LivingEntity) (Object) this;
        if (!(living instanceof Player player) || !DirectedGravityKernel.isActive(player)) return;
        AABB box = DirectedGravityKernel.makeBoundingBox(player, player.getDimensions(pose), player.position());
        cir.setReturnValue(player.level().noBlockCollision(player, box));
    }
}
