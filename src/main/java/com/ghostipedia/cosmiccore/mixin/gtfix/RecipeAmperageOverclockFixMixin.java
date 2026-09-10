package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IRecipeTierBoostMachine;
import com.ghostipedia.cosmiccore.common.power.recipe.RecipeAmperageOverclock;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.RecipeAmperageEnergyContainer;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = OverclockingLogic.class, remap = false)
public interface RecipeAmperageOverclockFixMixin {

    @ModifyVariable(
                    method = "getModifier(Lcom/gregtechceu/gtceu/api/machine/MetaMachine;Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;JZ)Lcom/gregtechceu/gtceu/api/recipe/modifier/ModifierFunction;",
                    at = @At("HEAD"),
                    argsOnly = true,
                    ordinal = 0,
                    require = 1,
                    expect = 1,
                    allow = 1)
    private static long cosmiccore$boundMultiblockOverclockVoltage(long maximumVoltage, MetaMachine machine,
                                                                   GTRecipe recipe) {
        if (machine instanceof WorkableElectricMultiblockMachine multiblock && !multiblock.isGenerator() &&
                machine instanceof IRecipeTierBoostMachine tierBoostMachine) {
            var energy = RecipeHelper.getRealEUt(recipe);
            return RecipeAmperageOverclock.getMaximumPacketVoltage(maximumVoltage, energy.getTotalEU(),
                    energy.voltage(), tierBoostMachine.getRecipeTierBoostState().maximumThroughput());
        }
        return maximumVoltage;
    }

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
        if (machine instanceof WorkableElectricMultiblockMachine multiblock && !multiblock.isGenerator()) {
            return RecipeAmperageOverclock.getEffectiveRecipeEUt(totalEUt, RecipeHelper.getRealEUt(recipe).voltage());
        }
        return totalEUt;
    }
}
