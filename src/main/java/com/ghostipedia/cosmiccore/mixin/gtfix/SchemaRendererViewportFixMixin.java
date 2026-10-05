package com.ghostipedia.cosmiccore.mixin.gtfix;

import brachy.modularui.drawable.schema.BaseSchemaRenderer;
import brachy.modularui.drawable.schema.Viewport;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = BaseSchemaRenderer.class, remap = false)
public class SchemaRendererViewportFixMixin {

    @Redirect(method = "draw(Lbrachy/modularui/screen/viewport/GuiContext;IIIILbrachy/modularui/theme/WidgetTheme;)V",
              at = @At(value = "INVOKE",
                       target = "Lbrachy/modularui/drawable/schema/Viewport;calculateOpenGLViewportFromRectangle(IIII)V"),
              require = 1)
    void cosmiccore$useGraphicsOrigin(Viewport viewport, int vx, int vy, int vw, int vh,
                                      GuiContext context, int x, int y, int width, int height,
                                      WidgetTheme theme) {
        Vector3f origin = context.getLastGraphicsPose().transformPosition(x, y, 0, new Vector3f());
        viewport.calculateOpenGLViewportFromRectangle((int) origin.x, (int) origin.y, vw, vh);
    }
}
