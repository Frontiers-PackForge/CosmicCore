package com.ghostipedia.cosmiccore.mixin.client;

import com.ghostipedia.cosmiccore.client.renderer.transmission.PowerTowerRideClient;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class PowerTowerWirePoseMixin {

    @Shadow
    public ModelPart rightArm;
    @Shadow
    public ModelPart leftArm;
    @Shadow
    public ModelPart rightLeg;
    @Shadow
    public ModelPart leftLeg;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void cosmiccore$hangFromWrench(LivingEntity entity, float limbSwing, float limbAmount, float age,
                                           float yaw, float pitch, CallbackInfo callback) {
        if (!PowerTowerRideClient.isRiding(entity.getUUID())) return;
        rightArm.xRot = leftArm.xRot = -3.0f;
        rightArm.yRot = leftArm.yRot = 0;
        rightArm.zRot = 0.12f;
        leftArm.zRot = -0.12f;
        rightLeg.xRot = leftLeg.xRot = 0.12f;
    }
}
