package com.ghostipedia.cosmiccore.mixin.gttweak.power;

import com.ghostipedia.cosmiccore.common.machine.generator.GeneratorFuelGrades;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleGeneratorMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SimpleGeneratorMachine.class, remap = false)
public abstract class SimpleGeneratorFuelGradeMixin {

    @Inject(method = "recipeModifier", at = @At("HEAD"), cancellable = true)
    private static void cosmiccore$gradeFuel(MetaMachine machine, GTRecipe recipe,
                                             CallbackInfoReturnable<ModifierFunction> cir) {
        if (machine instanceof SimpleGeneratorMachine generator &&
                GeneratorFuelGrades.isSingleblockRecipeType(recipe.recipeType.id)) {
            cir.setReturnValue(GeneratorFuelGrades.modifier(generator, recipe, generator.getTier(), 1,
                    generator.getOverclockVoltage(), 1));
        }
    }
}
