package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.client.gui.CompactAmountRenderer;

import net.neoforged.neoforge.fluids.FluidStack;

import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.widgets.slot.FluidSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FluidSlot.class, remap = false)
public abstract class FluidSlotAmountOverlayMixin {

    @Inject(method = "drawOverlay", at = @At("TAIL"))
    private void cosmiccore$drawTankAmount(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme,
                                           CallbackInfo ci) {
        FluidSlot slot = (FluidSlot) (Object) this;
        FluidStack fluid = slot.getFluidStack();
        if (fluid == null || fluid.isEmpty()) return;
        if (slot.getSyncHandler().phantom()) return;

        CompactAmountRenderer.drawFluidAmount(
                context.getGraphics(), -1, -1, slot.getArea().width, slot.getArea().height, fluid.getAmount());
    }
}
