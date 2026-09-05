package com.ghostipedia.cosmiccore.mixin.client;

import com.ghostipedia.cosmiccore.client.renderer.item.LeylineOrreryHandRenderer;

import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class LeylineOrreryHandMixin {

    @Inject(method = "renderItem", at = @At("HEAD"))
    private void cosmiccore$renderOrreryArm(LivingEntity entity, ItemStack stack, ItemDisplayContext context,
                                            boolean leftHand, PoseStack poseStack, MultiBufferSource buffer,
                                            int packedLight, CallbackInfo ci) {
        LeylineOrreryHandRenderer.renderArm(entity, stack, context, leftHand, poseStack, buffer, packedLight);
    }
}
