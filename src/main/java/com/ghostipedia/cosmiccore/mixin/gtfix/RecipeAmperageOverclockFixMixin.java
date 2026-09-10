package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.common.power.recipe.RecipeAmperageOverclock;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.RecipeAmperageEnergyContainer;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = OverclockingLogic.class, remap = false)
public interface RecipeAmperageOverclockFixMixin {

    @ModifyExpressionValue(
                           method = "getModifier(Lcom/gregtechceu/gtceu/api/machine/MetaMachine;Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;JZ)Lcom/gregtechceu/gtceu/api/recipe/modifier/ModifierFunction;",
                           at = @At(
                                    value = "INVOKE",
                                    target = "Lcom/gregtechceu/gtceu/api/recipe/ingredient/EnergyStack;getTotalEU()J"),
                           require = 1,
                           expect = 1,
                           allow = 1)
    private static long cosmiccore$keepRecipeAmperageOnThePacketTier(long totalEUt, MetaMachine machine,
                                                                     GTRecipe recipe) {
        if (machine instanceof WorkableTieredMachine tieredMachine &&
                tieredMachine.energyContainer instanceof RecipeAmperageEnergyContainer) {
            return RecipeAmperageOverclock.getEffectiveRecipeEUt(totalEUt, recipe.getInputEUt().voltage());
        }
        return totalEUt;
    }
}
