package com.ghostipedia.cosmiccore.mixin.gtfix;

import brachy.modularui.screen.EmbedHandler;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import com.llamalad7.mixinextras.sugar.Local;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EmbedHandler.class, remap = false)
public class EmbedHandlerSecondaryPanelTransformFixMixin {

    @Inject(
            method = "drawEmbed(Lbrachy/modularui/screen/ModularScreen;Lnet/minecraft/client/gui/GuiGraphics;FLjava/util/function/Predicate;)V",
            at = @At(
                     value = "INVOKE",
                     target = "Lbrachy/modularui/screen/ModularPanel;transform(Ljava/util/function/BiConsumer;)Lbrachy/modularui/widget/Widget;",
                     shift = At.Shift.AFTER),
            require = 1)
    private static void cosmiccore$transformSecondaryPanels(ModularScreen screen,
                                                            net.minecraft.client.gui.GuiGraphics graphics,
                                                            float partialTicks,
                                                            java.util.function.Predicate<net.minecraft.client.gui.components.Renderable> vanillaElementFilter,
                                                            CallbackInfo ci,
                                                            @Local Matrix4f pose) {
        ModularPanel<?> mainPanel = screen.getMainPanel();
        for (ModularPanel<?> panel : screen.getPanelManager().getOpenPanels()) {
            if (panel == mainPanel) continue;
            panel.transform((ignored, viewport) -> viewport.multiply(pose));
        }
    }
}
