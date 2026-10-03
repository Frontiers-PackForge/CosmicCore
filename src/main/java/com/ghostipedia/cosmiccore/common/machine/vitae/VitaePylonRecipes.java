package com.ghostipedia.cosmiccore.common.machine.vitae;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;

public final class VitaePylonRecipes {

    private VitaePylonRecipes() {}

    public static int parallelLimit(int altarLevel, int requiredLevel) {
        if (requiredLevel < 4 || requiredLevel > 6 || altarLevel < requiredLevel || altarLevel > 6) return 0;
        return 4 << (2 * (altarLevel - requiredLevel));
    }

    public static int limitByEnergy(EnergyStack energy, long stored, int limit) {
        if (energy.voltage() < 0 || energy.amperage() < 1 || stored < 0) return 0;
        try {
            long cost = Math.multiplyExact(energy.voltage(), energy.amperage());
            return cost == 0 ? limit : (int) Math.min(limit, stored / cost);
        } catch (ArithmeticException overflow) {
            return 0;
        }
    }

    public static ModifierFunction parallel(int amount) {
        if (amount < 1) return ModifierFunction.NULL;
        ModifierFunction contents = ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(amount)).parallels(amount).build();
        return recipe -> {
            GTRecipe result = contents.apply(recipe);
            EnergyStack energy = recipe.getInputEUt();
            if (energy.voltage() > 0) {
                try {
                    long amperage = Math.multiplyExact(energy.amperage(), amount);
                    Math.multiplyExact(energy.voltage(), amperage);
                    EURecipeCapability.putEUContent(result.tickInputs, new EnergyStack(energy.voltage(), amperage));
                } catch (ArithmeticException overflow) {
                    return null;
                }
            }
            return result;
        };
    }
}
