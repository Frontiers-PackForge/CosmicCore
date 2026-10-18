package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.client.renderer.machine.impl.QuantumChestItemRender;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(QuantumChestItemRender.class)
public abstract class QuantumChestItemOriginFixMixin {

    @WrapWithCondition(method = "renderByItem",
                       at = @At(value = "INVOKE",
                                target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V",
                                ordinal = 0),
                       require = 1)
    private boolean cosmiccore$preserveBlockModelOrigin(PoseStack poseStack, float x, float y, float z) {
        return false;
    }
}
