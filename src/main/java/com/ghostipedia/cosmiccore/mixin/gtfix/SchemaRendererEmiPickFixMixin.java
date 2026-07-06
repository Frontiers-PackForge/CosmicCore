package com.ghostipedia.cosmiccore.mixin.gtfix;

import brachy.modularui.drawable.schema.BaseSchemaRenderer;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BaseSchemaRenderer.class, remap = false)
public class SchemaRendererEmiPickFixMixin {

    @Unique
    private int cosmiccore$pickOffsetX;

    @Unique
    private int cosmiccore$pickOffsetY;

    @Inject(method = "draw(Lbrachy/modularui/screen/viewport/GuiContext;IIIILbrachy/modularui/theme/WidgetTheme;)V",
            at = @At("HEAD"),
            remap = false)
    private void cosmiccore$capturePickOffset(GuiContext context, int x, int y, int width, int height,
                                              WidgetTheme widgetTheme, CallbackInfo ci) {
        Matrix4f pose = context.getLastGraphicsPose();
        this.cosmiccore$pickOffsetX = (int) pose.m30() + context.transformX(-x, -y) - context.transformX(0, 0);
        this.cosmiccore$pickOffsetY = (int) pose.m31() + context.transformY(-x, -y) - context.transformY(0, 0);
    }

    @ModifyArg(method = "draw(Lbrachy/modularui/screen/viewport/GuiContext;IIIILbrachy/modularui/theme/WidgetTheme;)V",
               at = @At(value = "INVOKE",
                        target = "Lbrachy/modularui/drawable/schema/BaseSchemaRenderer;rayTrace(IIII)Lnet/minecraft/world/phys/BlockHitResult;"),
               index = 0,
               remap = false)
    private int cosmiccore$correctPickX(int mouseX) {
        return mouseX - this.cosmiccore$pickOffsetX;
    }

    @ModifyArg(method = "draw(Lbrachy/modularui/screen/viewport/GuiContext;IIIILbrachy/modularui/theme/WidgetTheme;)V",
               at = @At(value = "INVOKE",
                        target = "Lbrachy/modularui/drawable/schema/BaseSchemaRenderer;rayTrace(IIII)Lnet/minecraft/world/phys/BlockHitResult;"),
               index = 1,
               remap = false)
    private int cosmiccore$correctPickY(int mouseY) {
        return mouseY - this.cosmiccore$pickOffsetY;
    }
}
