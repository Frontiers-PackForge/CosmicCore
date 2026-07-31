package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = GTRecipeTypes.class, remap = false)
public abstract class DistilleryOutputFluidFixMixin {

    @ModifyArg(
               method = "lambda$static$80(Lcom/gregtechceu/gtceu/data/recipe/builder/GTRecipeBuilder;Lnet/minecraft/data/recipes/RecipeOutput;)V",
               at = @At(
                        value = "INVOKE",
                        target = "Lnet/neoforged/neoforge/fluids/crafting/SizedFluidIngredient;<init>(Lnet/neoforged/neoforge/fluids/crafting/FluidIngredient;I)V",
                        ordinal = 1,
                        remap = false),
               index = 0,
               require = 1,
               remap = false)
    private static FluidIngredient cosmiccore$useDistilleryOutputIngredient(FluidIngredient ignored,
                                                                            @Local(name = "output") SizedFluidIngredient output) {
        return output.ingredient();
    }
}
