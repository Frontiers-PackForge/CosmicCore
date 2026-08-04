package com.ghostipedia.cosmiccore.mixin.gravity.client;

import com.ghostipedia.cosmiccore.client.gravity.DirectedGravityClientState;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {

    @Inject(method = "setupRotations", at = @At("HEAD"))
    private void cosmicCore$applyDirectedRotation(
                                                  AbstractClientPlayer entity,
                                                  PoseStack poseStack,
                                                  float bob,
                                                  float yBodyRot,
                                                  float partialTick,
                                                  float scale,
                                                  CallbackInfo ci) {
        Quaternionf rotation = DirectedGravityClientState.modelRotation(entity, partialTick);
        if (rotation != null) poseStack.mulPose(rotation);
    }
}
