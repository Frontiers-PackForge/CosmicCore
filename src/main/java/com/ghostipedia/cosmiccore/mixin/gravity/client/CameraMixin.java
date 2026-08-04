package com.ghostipedia.cosmiccore.mixin.gravity.client;

import com.ghostipedia.cosmiccore.client.gravity.DirectedGravityClientState;

import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    private float eyeHeight;

    @Shadow
    private float eyeHeightOld;

    @Shadow
    protected abstract void setRotation(float yRot, float xRot, float roll);

    @Shadow
    protected abstract void setPosition(Vec3 position);

    @Inject(
            method = "setup",
            at = @At(
                     value = "INVOKE",
                     target = "Lnet/minecraft/client/Camera;setRotation(FFF)V",
                     ordinal = 0,
                     shift = At.Shift.AFTER))
    private void cosmicCore$rotatePrimaryCamera(
                                                BlockGetter level,
                                                Entity entity,
                                                boolean detached,
                                                boolean thirdPersonReverse,
                                                float partialTick,
                                                CallbackInfo ci) {
        if (detached && thirdPersonReverse) return;
        if (!detached && entity instanceof LivingEntity livingEntity && livingEntity.isSleeping()) return;
        cosmicCore$applyDirectedRotation(entity, partialTick);
    }

    @Inject(
            method = "setup",
            at = @At(
                     value = "INVOKE",
                     target = "Lnet/minecraft/client/Camera;setRotation(FFF)V",
                     ordinal = 1,
                     shift = At.Shift.AFTER))
    private void cosmicCore$rotateReverseCamera(
                                                BlockGetter level,
                                                Entity entity,
                                                boolean detached,
                                                boolean thirdPersonReverse,
                                                float partialTick,
                                                CallbackInfo ci) {
        if (detached && thirdPersonReverse) cosmicCore$applyDirectedRotation(entity, partialTick);
    }

    @Inject(
            method = "setup",
            at = @At(
                     value = "INVOKE",
                     target = "Lnet/minecraft/client/Camera;setRotation(FF)V",
                     shift = At.Shift.AFTER))
    private void cosmicCore$rotateSleepingCamera(
                                                 BlockGetter level,
                                                 Entity entity,
                                                 boolean detached,
                                                 boolean thirdPersonReverse,
                                                 float partialTick,
                                                 CallbackInfo ci) {
        if (!detached && entity instanceof LivingEntity livingEntity && livingEntity.isSleeping()) {
            cosmicCore$applyDirectedRotation(entity, partialTick);
        }
    }

    @Inject(
            method = "setup",
            at = @At(
                     value = "INVOKE",
                     target = "Lnet/minecraft/client/Camera;setPosition(DDD)V",
                     ordinal = 0,
                     shift = At.Shift.AFTER))
    private void cosmicCore$moveDirectedEye(
                                            BlockGetter level,
                                            Entity entity,
                                            boolean detached,
                                            boolean thirdPersonReverse,
                                            float partialTick,
                                            CallbackInfo ci) {
        if (!(entity instanceof Player player)) return;
        Vec3 eyeOffset = DirectedGravityClientState.eyeOffset(
                player, Mth.lerp(partialTick, eyeHeightOld, eyeHeight));
        if (eyeOffset == null) return;

        Vec3 base = new Vec3(
                Mth.lerp((double) partialTick, entity.xo, entity.getX()),
                Mth.lerp((double) partialTick, entity.yo, entity.getY()),
                Mth.lerp((double) partialTick, entity.zo, entity.getZ()));
        setPosition(base.add(eyeOffset));
    }

    @Unique
    private void cosmicCore$applyDirectedRotation(Entity entity, float partialTick) {
        if (!(entity instanceof Player player)) return;
        Quaternionf cameraRotation = ((Camera) (Object) this).rotation();
        Quaternionf composed = DirectedGravityClientState.cameraRotation(player, cameraRotation, partialTick);
        if (composed == null) return;
        Vector3f angles = composed.getEulerAnglesYXZ(new Vector3f());
        setRotation(
                (float) Math.toDegrees(Math.PI - angles.y),
                (float) Math.toDegrees(-angles.x),
                (float) Math.toDegrees(-angles.z));
    }
}
