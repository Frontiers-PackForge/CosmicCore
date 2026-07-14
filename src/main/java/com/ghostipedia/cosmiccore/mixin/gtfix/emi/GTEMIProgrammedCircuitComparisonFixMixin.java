package com.ghostipedia.cosmiccore.mixin.gtfix.emi;

import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.gregtechceu.gtceu.integration.recipeviewer.emi.GTEMIPlugin", remap = false)
public class GTEMIProgrammedCircuitComparisonFixMixin {

    @Redirect(
              method = "register",
              at = @At(value = "INVOKE",
                       target = "Ldev/emi/emi/api/EmiRegistry;removeEmiStacks(Ldev/emi/emi/api/stack/EmiStack;)V"),
              remap = false)
    private void cosmiccore$keepProgrammedCircuitVisible(EmiRegistry registry, EmiStack stack) {}

    @Inject(method = "register", at = @At("TAIL"), remap = false)
    private void cosmiccore$matchUnconfiguredCircuit(EmiRegistry registry, CallbackInfo ci) {
        registry.setDefaultComparison(GTItems.PROGRAMMED_CIRCUIT.asItem(),
                Comparison.compareData(stack -> stack.getOrDefault(GTDataComponents.CIRCUIT_CONFIG.get(), 0)));
    }
}
