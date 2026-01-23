package com.ghostipedia.cosmiccore.mixin.quake;

import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.QuakeMovementHandler;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class QuakeMovementMixin {

    @Unique
    private Vec3 cosmiccore$preTravelVelocity = null;

    @Unique
    private int cosmiccore$airStrafeActiveTicks = 0;

    @Unique
    private static final int AIR_STRAFE_GRACE_TICKS = 10;

    @Inject(method = "travel", at = @At("HEAD"))
    private void cosmiccore$beforeTravel(Vec3 movementInput, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (!(entity instanceof Player player)) {
            cosmiccore$preTravelVelocity = null;
            return;
        }

        if (!player.level().isClientSide()) {
            cosmiccore$preTravelVelocity = null;
            return;
        }

        if (!QuakeMovementHandler.getClientHasQuakeMovement()) {
            cosmiccore$preTravelVelocity = null;
            return;
        }

        if (player.isInWater() || player.isInLava() || player.getAbilities().flying || player.isFallFlying()) {
            cosmiccore$preTravelVelocity = null;
            return;
        }

        cosmiccore$preTravelVelocity = player.getDeltaMovement();
    }

    @Inject(method = "travel", at = @At("RETURN"))
    private void cosmiccore$afterTravel(Vec3 movementInput, CallbackInfo ci) {
        if (cosmiccore$preTravelVelocity == null) return;

        LivingEntity entity = (LivingEntity) (Object) this;
        if (!(entity instanceof Player player)) return;

        Vec3 preVel = cosmiccore$preTravelVelocity;
        Vec3 postVel = player.getDeltaMovement();

        double preSpeed = Math.sqrt(preVel.x * preVel.x + preVel.z * preVel.z);
        double postSpeed = Math.sqrt(postVel.x * postVel.x + postVel.z * postVel.z);

        cosmiccore$preTravelVelocity = null;

        float forwardInput = player.zza;
        float strafeInput = player.xxa;
        boolean hasMovementInput = Math.abs(forwardInput) > 0.01 || Math.abs(strafeInput) > 0.01;
        boolean hasStrafeInput = Math.abs(strafeInput) > 0.01;
        boolean onGroundNow = player.onGround();

        // Air strafe detection
        if (!onGroundNow && hasStrafeInput) {
            cosmiccore$airStrafeActiveTicks = AIR_STRAFE_GRACE_TICKS;
        } else if (cosmiccore$airStrafeActiveTicks > 0) {
            cosmiccore$airStrafeActiveTicks--;
        }
        boolean isAirStrafing = cosmiccore$airStrafeActiveTicks > 0;

        // Braking detection
        boolean isBraking = false;
        if (hasMovementInput && preSpeed > 0.1) {
            float yaw = player.getYRot() * ((float) Math.PI / 180f);
            double wishX = -Math.sin(yaw) * forwardInput + Math.cos(yaw) * strafeInput;
            double wishZ = Math.cos(yaw) * forwardInput + Math.sin(yaw) * strafeInput;
            double velX = preVel.x / preSpeed;
            double velZ = preVel.z / preSpeed;
            double dot = wishX * velX + wishZ * velZ;
            isBraking = dot < -0.3;
        }

        boolean wantsToStop = onGroundNow && !hasMovementInput;
        if (isBraking || wantsToStop) return;

        // Tiered speed cap system
        double bhopSoftCap = QuakeMovementHandler.getBhopSoftCap();
        double hardCap = QuakeMovementHandler.getHardCapSpeed();
        double effectiveCap = isAirStrafing ? hardCap : bhopSoftCap;

        double targetSpeed;

        if (postSpeed >= preSpeed) {
            targetSpeed = Math.min(postSpeed, effectiveCap);
        } else {
            double speedLost = preSpeed - postSpeed;

            if (preSpeed <= bhopSoftCap) {
                double restorationFactor = 0.95;
                targetSpeed = postSpeed + (speedLost * restorationFactor);
                targetSpeed = Math.min(targetSpeed, bhopSoftCap);
            } else if (isAirStrafing) {
                double restorationFactor = 0.97;
                targetSpeed = postSpeed + (speedLost * restorationFactor);
                targetSpeed = Math.min(targetSpeed, hardCap);
            } else {
                double decayRate = 0.92;
                double excessSpeed = preSpeed - bhopSoftCap;
                double newExcess = excessSpeed * decayRate;
                targetSpeed = bhopSoftCap + newExcess;

                double frictionRestore = speedLost * 0.5;
                targetSpeed = Math.max(targetSpeed, postSpeed + frictionRestore);
                targetSpeed = Math.min(targetSpeed, preSpeed);
            }
        }

        if (targetSpeed < 0.36) return;
        if (Math.abs(targetSpeed - postSpeed) < 0.001) return;

        if (postSpeed > 0.001) {
            double scale = targetSpeed / postSpeed;
            player.setDeltaMovement(postVel.x * scale, postVel.y, postVel.z * scale);
        } else if (preSpeed > 0.001) {
            double scale = targetSpeed / preSpeed;
            player.setDeltaMovement(preVel.x * scale, postVel.y, preVel.z * scale);
        }
    }
}
