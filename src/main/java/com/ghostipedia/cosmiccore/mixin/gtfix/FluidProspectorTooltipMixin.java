package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.item.component.prospector.ProspectorMode;

import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(targets = "com.gregtechceu.gtceu.api.item.component.prospector.ProspectorMode$2", remap = false)
public abstract class FluidProspectorTooltipMixin {

    @Inject(method = "appendTooltips", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$yieldOnly(List<ProspectorMode.FluidInfo[]> items, List<Component> tooltips,
                                      String selected, CallbackInfo ci) {
        for (var cell : items) {
            for (var item : cell) {
                tooltips.add(Component.translatable("cosmiccore.fluid_drill.prospected_yield",
                        item.fluid().getFluidType().getDescription(item.asStack()), item.yield()));
            }
        }
        ci.cancel();
    }
}
