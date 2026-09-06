package com.ghostipedia.cosmiccore.mixin.client;

import com.ghostipedia.cosmiccore.client.renderer.item.LeylineOrreryHandRenderer;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;

import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemInHandRenderer.class)
public abstract class LeylineOrreryHandMixin {

    @WrapMethod(method = "renderItem")
    private void cosmiccore$renderOrreryArm(LivingEntity entity, ItemStack stack, ItemDisplayContext context,
                                            boolean leftHand, PoseStack poseStack, MultiBufferSource buffer,
                                            int packedLight, Operation<Void> original) {
        if (!context.firstPerson() || !stack.is(CosmicItems.LEYLINE_ORRERY.get())) {
            original.call(entity, stack, context, leftHand, poseStack, buffer, packedLight);
            return;
        }
        LeylineOrreryHandRenderer.renderArm(entity, stack, context, leftHand, poseStack, buffer, packedLight);
        poseStack.pushPose();
        try {
            LeylineOrreryHandRenderer.offsetTool(entity, stack, context, leftHand, poseStack);
            original.call(entity, stack, context, leftHand, poseStack, buffer, packedLight);
        } finally {
            poseStack.popPose();
        }
    }
}
