package com.ghostipedia.cosmiccore.mixin.emi;

import com.gregtechceu.gtceu.integration.recipeviewer.emi.orevein.GTBedrockOreEmiCategory.GTBedrockOre;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = GTBedrockOre.class, remap = false)
public class BedrockOreIOCacheMixin {

    @Unique
    private List<EmiIngredient> cosmiccore$inputs;
    @Unique
    private List<EmiStack> cosmiccore$outputs;

    @Inject(method = "getInputs", at = @At("HEAD"), cancellable = true, remap = false)
    private void cosmiccore$cachedInputs(CallbackInfoReturnable<List<EmiIngredient>> cir) {
        if (this.cosmiccore$inputs != null) {
            cir.setReturnValue(this.cosmiccore$inputs);
        }
    }

    @Inject(method = "getInputs", at = @At("RETURN"), remap = false)
    private void cosmiccore$storeInputs(CallbackInfoReturnable<List<EmiIngredient>> cir) {
        this.cosmiccore$inputs = cir.getReturnValue();
    }

    @Inject(method = "getOutputs", at = @At("HEAD"), cancellable = true, remap = false)
    private void cosmiccore$cachedOutputs(CallbackInfoReturnable<List<EmiStack>> cir) {
        if (this.cosmiccore$outputs != null) {
            cir.setReturnValue(this.cosmiccore$outputs);
        }
    }

    @Inject(method = "getOutputs", at = @At("RETURN"), remap = false)
    private void cosmiccore$storeOutputs(CallbackInfoReturnable<List<EmiStack>> cir) {
        this.cosmiccore$outputs = cir.getReturnValue();
    }
}
