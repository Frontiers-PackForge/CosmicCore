package com.ghostipedia.cosmiccore.mixin.gttweak.emi;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.gregtechceu.gtceu.integration.recipeviewer.emi.GTEMIPlugin", remap = false)
public class GTEMIProgrammedCircuitVisibilityMixin {

    @Redirect(
              method = "register",
              at = @At(value = "INVOKE",
                       target = "Ldev/emi/emi/api/EmiRegistry;removeEmiStacks(Ldev/emi/emi/api/stack/EmiStack;)V"),
              remap = false)
    private void cosmiccore$keepProgrammedCircuitVisible(EmiRegistry registry, EmiStack stack) {}
}
