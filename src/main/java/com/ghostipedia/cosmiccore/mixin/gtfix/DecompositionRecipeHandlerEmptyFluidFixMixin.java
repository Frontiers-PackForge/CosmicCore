package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.data.recipe.generated.DecompositionRecipeHandler;

import net.minecraft.data.recipes.RecipeOutput;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DecompositionRecipeHandler.class, remap = false)
public class DecompositionRecipeHandlerEmptyFluidFixMixin {

    @Inject(method = "processDecomposition", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cosmiccore$skipEmptyFluidDecomposition(RecipeOutput provider, Material material,
                                                               CallbackInfo ci) {
        if (!material.hasProperty(PropertyKey.DUST) &&
                (!material.hasProperty(PropertyKey.FLUID) || material.getFluid(1000).isEmpty())) {
            ci.cancel();
            return;
        }
        for (MaterialStack component : material.getMaterialComponents()) {
            Material componentMaterial = component.material();
            if (!componentMaterial.hasProperty(PropertyKey.DUST) && componentMaterial.hasProperty(PropertyKey.FLUID) &&
                    componentMaterial.getFluid((int) (1000 * component.amount())).isEmpty()) {
                ci.cancel();
                return;
            }
        }
    }
}
