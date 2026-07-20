package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.client.compat.modularui.SchemaRenderState;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;

import brachy.modularui.drawable.schema.BaseSchemaRenderer;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BaseSchemaRenderer.class, remap = false)
public abstract class SchemaRendererStateCleanupMixin {

    @Unique
    private boolean cosmiccore$cameraActive;

    @Unique
    private boolean cosmiccore$guiPoseActive;

    @Unique
    private SchemaRenderState cosmiccore$renderState;

    @Inject(
            method = "draw(Lbrachy/modularui/screen/viewport/GuiContext;IIIILbrachy/modularui/theme/WidgetTheme;)V",
            at = @At(
                     value = "INVOKE",
                     target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
                     shift = At.Shift.AFTER),
            remap = false)
    private void cosmiccore$markGuiPoseActive(GuiContext context, int x, int y, int width, int height,
                                              WidgetTheme widgetTheme, CallbackInfo ci) {
        this.cosmiccore$guiPoseActive = true;
    }

    @Inject(
            method = "draw(Lbrachy/modularui/screen/viewport/GuiContext;IIIILbrachy/modularui/theme/WidgetTheme;)V",
            at = @At(
                     value = "INVOKE",
                     target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V",
                     shift = At.Shift.AFTER),
            remap = false)
    private void cosmiccore$markGuiPoseReset(GuiContext context, int x, int y, int width, int height,
                                             WidgetTheme widgetTheme, CallbackInfo ci) {
        this.cosmiccore$guiPoseActive = false;
    }

    @Inject(
            method = "setupCamera(II)V",
            at = @At(
                     value = "INVOKE",
                     target = "Lorg/joml/Matrix4fStack;pushMatrix()Lorg/joml/Matrix4fStack;",
                     shift = At.Shift.AFTER),
            remap = false)
    private void cosmiccore$markCameraActive(int width, int height, CallbackInfo ci) {
        this.cosmiccore$cameraActive = true;
    }

    @Inject(
            method = "resetCamera()V",
            at = @At(
                     value = "INVOKE",
                     target = "Lorg/joml/Matrix4fStack;popMatrix()Lorg/joml/Matrix4fStack;",
                     shift = At.Shift.AFTER),
            remap = false)
    private void cosmiccore$markCameraReset(CallbackInfo ci) {
        this.cosmiccore$cameraActive = false;
    }

    @WrapOperation(
                   method = "renderBlocks",
                   at = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/RenderType;setupRenderState()V"),
                   remap = false)
    private void cosmiccore$captureRenderTypeShader(RenderType renderType, Operation<Void> original) {
        original.call(renderType);
        SchemaRenderState state = this.cosmiccore$renderState;
        if (state != null) {
            state.captureShader(RenderSystem.getShader());
        }
    }

    @WrapMethod(
                method = "draw(Lbrachy/modularui/screen/viewport/GuiContext;IIIILbrachy/modularui/theme/WidgetTheme;)V",
                remap = false)
    private void cosmiccore$restoreRenderState(GuiContext context, int x, int y, int width, int height,
                                               WidgetTheme widgetTheme, Operation<Void> original) {
        SchemaRenderState previousState = this.cosmiccore$renderState;
        SchemaRenderState state = new SchemaRenderState();
        this.cosmiccore$renderState = state;
        try {
            original.call(context, x, y, width, height, widgetTheme);
        } finally {
            if (this.cosmiccore$cameraActive) {
                this.cosmiccore$cameraActive = false;
                RenderSystem.getModelViewStack().popMatrix();
            }
            if (this.cosmiccore$guiPoseActive) {
                this.cosmiccore$guiPoseActive = false;
                context.graphicsPose().popPose();
            }
            ModelBlockRenderer.clearCache();
            state.restore();
            this.cosmiccore$renderState = previousState;
        }
    }
}
