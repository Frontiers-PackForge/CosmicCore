package com.ghostipedia.cosmiccore.mixin.gtfix;

import net.minecraft.util.Mth;

import brachy.modularui.widgets.SchemaWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SchemaWidget.class, remap = false)
public class SchemaWidgetZoomMixin {

    @Inject(method = "onMouseScrolled(DD)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private void cosmiccore$proportionalZoom(double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
        SchemaWidget self = (SchemaWidget) (Object) this;
        if (!self.isEnableScaling()) return;

        float zoomed = Mth.clamp(self.getScale() * (float) Math.pow(1.25, -scrollY), 0.1F, 10000F);
        self.scale(zoomed);
        cir.setReturnValue(true);
    }
}
