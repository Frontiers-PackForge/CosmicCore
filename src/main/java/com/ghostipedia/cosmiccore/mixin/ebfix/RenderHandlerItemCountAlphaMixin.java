package com.ghostipedia.cosmiccore.mixin.ebfix;

import neoforge.nl.requios.effortlessbuilding.render.RenderHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(RenderHandler.class)
public abstract class RenderHandlerItemCountAlphaMixin {

    @ModifyArg(
               method = "drawItemStack",
               at = @At(
                        value = "INVOKE",
                        target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I"),
               index = 4)
    private static int cosmiccore$restoreCountAlpha(int color) {
        return 0xFF000000 | color;
    }
}
