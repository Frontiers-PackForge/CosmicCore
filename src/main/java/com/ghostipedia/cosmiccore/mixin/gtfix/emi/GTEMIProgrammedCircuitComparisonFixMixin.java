package com.ghostipedia.cosmiccore.mixin.gtfix.emi;

import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.Comparison;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.gregtechceu.gtceu.integration.recipeviewer.emi.GTEMIPlugin", remap = false)
public class GTEMIProgrammedCircuitComparisonFixMixin {

    @Inject(method = "register", at = @At("TAIL"), remap = false)
    private void cosmiccore$matchUnconfiguredCircuit(EmiRegistry registry, CallbackInfo ci) {
        registry.setDefaultComparison(GTItems.PROGRAMMED_CIRCUIT.asItem(),
                Comparison.compareData(stack -> stack.getOrDefault(GTDataComponents.CIRCUIT_CONFIG.get(), 0)));
    }
}
