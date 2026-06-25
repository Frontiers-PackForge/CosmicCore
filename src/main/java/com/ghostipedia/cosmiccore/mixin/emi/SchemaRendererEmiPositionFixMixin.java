package com.ghostipedia.cosmiccore.mixin.emi;

import brachy.modularui.drawable.schema.BaseSchemaRenderer;
import brachy.modularui.drawable.schema.Viewport;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = BaseSchemaRenderer.class, remap = false)
public class SchemaRendererEmiPositionFixMixin {

    @Redirect(method = "draw(Lbrachy/modularui/screen/viewport/GuiContext;IIIILbrachy/modularui/theme/WidgetTheme;)V",
              at = @At(value = "INVOKE",
                       target = "Lbrachy/modularui/drawable/schema/Viewport;calculateOpenGLViewportFromRectangle(IIII)V"),
              remap = false,
              require = 0)
    private void cosmiccore$followEmiPanel(Viewport viewport, int vx, int vy, int vw, int vh,
                                           GuiContext context, int x, int y, int width, int height,
                                           WidgetTheme theme) {
        Matrix4f pose = context.getLastGraphicsPose();
        int internalX = context.transformX(0, 0);
        int internalY = context.transformY(0, 0);
        int correctedX = (int) pose.m30() + (vx - internalX);
        int correctedY = (int) pose.m31() + (vy - internalY);

        viewport.calculateOpenGLViewportFromRectangle(correctedX, correctedY, vw, vh);
    }
}
