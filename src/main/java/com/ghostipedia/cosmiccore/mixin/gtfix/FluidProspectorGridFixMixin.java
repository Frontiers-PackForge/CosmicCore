package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.item.component.prospector.ProspectorMode;

import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.screen.viewport.GuiContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.gregtechceu.gtceu.api.item.component.prospector.ProspectorMode$2", remap = false)
public abstract class FluidProspectorGridFixMixin {

    @Inject(method = "drawSpecialGrid(Lbrachy/modularui/screen/viewport/GuiContext;[Lcom/gregtechceu/gtceu/api/item/component/prospector/ProspectorMode$FluidInfo;IIII)V",
            at = @At("HEAD"),
            cancellable = true)
    private void cosmiccore$drawCell(GuiContext context, ProspectorMode.FluidInfo[] items, int x, int y,
                                     int width, int height, CallbackInfo ci) {
        if (items.length > 0) {
            GuiDraw.drawFluidTexture(context.getGraphics(), items[0].asStack(), x, y,
                    width - 1, height - 1, context.getCurrentDrawingZ());
        }
        ci.cancel();
    }
}
