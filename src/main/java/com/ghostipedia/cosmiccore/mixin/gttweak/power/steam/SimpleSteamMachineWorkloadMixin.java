package com.ghostipedia.cosmiccore.mixin.gttweak.power.steam;

import com.ghostipedia.cosmiccore.common.power.steam.SteamRecipeExecution;
import com.ghostipedia.cosmiccore.common.power.steam.WorkConservingSteamEnergyRecipeHandler;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.machine.steam.SteamEnergyRecipeHandler;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.recipe.condition.VentCondition;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SimpleSteamMachine.class, remap = false)
public abstract class SimpleSteamMachineWorkloadMixin {

    @Inject(
            method = "recipeModifier(Lcom/gregtechceu/gtceu/api/machine/MetaMachine;Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;)Lcom/gregtechceu/gtceu/api/recipe/modifier/ModifierFunction;",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            expect = 1,
            allow = 1)
    private static void cosmiccore$normalizeSteamWorkload(MetaMachine machine, GTRecipe recipe,
                                                          CallbackInfoReturnable<ModifierFunction> cir) {
        if (!(machine instanceof SimpleSteamMachine steamMachine)) {
            cir.setReturnValue(RecipeModifier.nullWrongType(SimpleSteamMachine.class, machine));
            return;
        }
        if (RecipeHelper.getRecipeEUtTier(recipe) > GTValues.LV ||
                !steamMachine.getExhaustVentTrait().checkVenting() || SteamRecipeExecution.resolve(recipe) == null) {
            cir.setReturnValue(ModifierFunction.NULL);
            return;
        }

        boolean highPressure = steamMachine.isHighPressure();
        cir.setReturnValue(candidate -> {
            GTRecipe runtimeRecipe = SteamRecipeExecution.createRuntimeRecipe(candidate, highPressure);
            if (runtimeRecipe == null) {
                return null;
            }
            runtimeRecipe.conditions.add(VentCondition.INSTANCE);
            return runtimeRecipe;
        });
    }

    @Redirect(
              method = "onLoad()V",
              at = @At(
                       value = "NEW",
                       target = "(Lcom/gregtechceu/gtceu/api/machine/trait/notifiable/NotifiableFluidTank;D)Lcom/gregtechceu/gtceu/api/machine/steam/SteamEnergyRecipeHandler;"),
              require = 1,
              expect = 1,
              allow = 1)
    private SteamEnergyRecipeHandler cosmiccore$createWorkConservingHandler(NotifiableFluidTank steamTank,
                                                                            double conversionRate) {
        SimpleSteamMachine machine = (SimpleSteamMachine) (Object) this;
        return new WorkConservingSteamEnergyRecipeHandler(
                steamTank, conversionRate, machine.getRecipeLogic());
    }
}
