package com.ghostipedia.cosmiccore.common.power.steam;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.steam.SteamEnergyRecipeHandler;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.List;

public final class WorkConservingSteamEnergyRecipeHandler extends SteamEnergyRecipeHandler {

    private final RecipeLogic recipeLogic;

    public WorkConservingSteamEnergyRecipeHandler(NotifiableFluidTank steamTank, double conversionRate,
                                                  RecipeLogic recipeLogic) {
        super(steamTank, conversionRate);
        this.recipeLogic = recipeLogic;
    }

    @Override
    public List<EnergyStack> handleRecipeInner(IO io, GTRecipe recipe, List<EnergyStack> left, boolean simulate) {
        if (io != IO.IN) {
            return super.handleRecipeInner(io, recipe, left, simulate);
        }

        SteamRecipeExecution.RuntimePlan plan = SteamRecipeExecution.runtimePlan(recipe);
        if (plan == null) {
            return SteamRecipeExecution.hasRuntimePlanMarker(recipe) ? left :
                    super.handleRecipeInner(io, recipe, left, simulate);
        }

        int progress = recipe == this.recipeLogic.getLastRecipe() ? this.recipeLogic.getProgress() : 0;
        int steamForTick = plan.steamForProgress(progress);
        if (steamForTick <= 0) {
            return left;
        }

        for (var iterator = left.listIterator(); iterator.hasNext();) {
            EnergyStack stack = iterator.next();
            if (stack.isEmpty()) {
                iterator.remove();
                continue;
            }

            var steam = SizedFluidIngredient.of(GTMaterials.Steam.getFluidTag(), steamForTick);
            var steamRequest = new ArrayList<SizedFluidIngredient>();
            steamRequest.add(steam);
            var remainingSteam = getSteamTank().handleRecipeInner(IO.IN, recipe, steamRequest, simulate);
            if (remainingSteam.isEmpty()) {
                iterator.remove();
            }
        }
        return left;
    }
}
