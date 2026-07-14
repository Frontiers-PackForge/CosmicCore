package com.ghostipedia.cosmiccore.mixin.drippy;

import net.minecraft.client.gui.screens.LoadingOverlay;

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = LoadingOverlay.class, priority = 900)
public class DrippyLoadingOverlayScaleFixMixin {

    @Redirect(
              method = "lambda$afterRenderDrippy$0(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
              at = @At(
                       value = "INVOKE",
                       target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"),
              remap = false)
    private void cosmiccore$applyUntrackedDrippyScale(PoseStack poseStack, float x, float y, float z) {
        poseStack.last().pose().scale(x, y, z);
    }
}
