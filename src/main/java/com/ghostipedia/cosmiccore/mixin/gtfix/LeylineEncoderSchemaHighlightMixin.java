package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.client.renderer.deployment.LeylineEncoderHighlight;

import net.minecraft.core.Direction;

import brachy.modularui.drawable.schema.BaseSchemaRenderer;
import brachy.modularui.drawable.schema.BlockHighlight;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.Color;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BaseSchemaRenderer.class, remap = false)
public abstract class LeylineEncoderSchemaHighlightMixin {

    @Unique
    private static final BlockHighlight COSMICCORE_LEYLINE_HIGHLIGHT = new BlockHighlight(
            Color.withAlpha(Color.RED.brighter(1), 0.95F), 1 / 24.0F);

    @Inject(
            method = "draw(Lbrachy/modularui/screen/viewport/GuiContext;IIIILbrachy/modularui/theme/WidgetTheme;)V",
            at = @At(
                     value = "INVOKE",
                     target = "Lbrachy/modularui/drawable/schema/BaseSchemaRenderer;renderWorld(Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;F)V",
                     shift = At.Shift.AFTER),
            remap = false)
    private void cosmiccore$drawLeylineSelection(GuiContext context, int x, int y, int width, int height,
                                                 WidgetTheme widgetTheme, CallbackInfo ci) {
        BaseSchemaRenderer renderer = (BaseSchemaRenderer) (Object) this;
        var pos = LeylineEncoderHighlight.active(renderer);
        if (pos != null) {
            COSMICCORE_LEYLINE_HIGHLIGHT.renderHighlight(renderer.createWorldRenderPose(), pos, Direction.UP,
                    renderer.camera().pos());
        }
    }
}
