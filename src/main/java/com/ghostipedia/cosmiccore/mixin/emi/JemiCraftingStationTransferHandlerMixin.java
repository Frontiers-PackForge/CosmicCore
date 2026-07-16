package com.ghostipedia.cosmiccore.mixin.emi;

import com.ghostipedia.cosmiccore.integration.recipes.emi.CraftingStationRecipeHandler;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.jemi.JemiRecipeHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = JemiRecipeHandler.class, remap = false)
public abstract class JemiCraftingStationTransferHandlerMixin {

    @Inject(method = "supportsRecipe", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$preferNativeCraftingStationHandler(EmiRecipe recipe,
                                                               CallbackInfoReturnable<Boolean> cir) {
        JemiRecipeHandler<?, ?> handler = (JemiRecipeHandler<?, ?>) (Object) this;
        if (CraftingStationRecipeHandler.shouldBypassJemi(handler.handler)) {
            cir.setReturnValue(false);
        }
    }
}
