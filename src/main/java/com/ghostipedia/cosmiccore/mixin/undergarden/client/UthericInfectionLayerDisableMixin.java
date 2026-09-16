package com.ghostipedia.cosmiccore.mixin.undergarden.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quek.undergarden.client.render.layer.UthericInfectionLayer;

@Mixin(value = UthericInfectionLayer.class, remap = false)
public class UthericInfectionLayerDisableMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    private void cosmiccore$disableInfectionEntityLayer(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                                        LivingEntity entity, float limbSwing, float limbSwingAmount,
                                                        float partialTick, float ageInTicks, float netHeadYaw,
                                                        float headPitch, CallbackInfo ci) {
        ci.cancel();
    }
}
