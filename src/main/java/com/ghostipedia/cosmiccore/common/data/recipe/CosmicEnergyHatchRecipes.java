package com.ghostipedia.cosmiccore.common.data.recipe;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicMachines;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.data.recipe.GTCraftingComponents;

import net.minecraft.data.recipes.RecipeOutput;

import java.util.Locale;
import java.util.Objects;

import static com.gregtechceu.gtceu.api.GTValues.HV;
import static com.gregtechceu.gtceu.api.GTValues.LV;
import static com.gregtechceu.gtceu.api.GTValues.OpV;
import static com.gregtechceu.gtceu.api.GTValues.UHV;
import static com.gregtechceu.gtceu.api.GTValues.UV;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLER_RECIPES;

public final class CosmicEnergyHatchRecipes {

    private CosmicEnergyHatchRecipes() {}

    public static void register(RecipeOutput provider) {
        for (int tier = LV; tier <= HV; tier++) {
            register64AEnergyHatch(provider, tier, true);
            register64AEnergyHatch(provider, tier, false);
        }

        int highestTier = GTCEuAPI.isHighTier() ? OpV : UHV;
        for (int tier = LV; tier <= highestTier; tier++) {
            register256AEnergyHatch(provider, tier, true);
            register256AEnergyHatch(provider, tier, false);
        }
    }

    private static void register64AEnergyHatch(RecipeOutput provider, int tier, boolean input) {
        MachineDefinition transformer = requireMachine(powerTransformer(tier), "power transformer", tier);
        MachineDefinition source = requireMachine(
                input ? CosmicMachines.ENERGY_INPUT_HATCH_16A[tier] :
                        CosmicMachines.ENERGY_OUTPUT_HATCH_16A[tier],
                input ? "16A energy hatch" : "16A dynamo hatch", tier);
        MachineDefinition result = requireMachine(
                input ? CosmicMachines.ENERGY_INPUT_HATCH_64A[tier] :
                        CosmicMachines.ENERGY_OUTPUT_HATCH_64A[tier],
                input ? "64A substation energy hatch" : "64A substation dynamo hatch", tier);

        ASSEMBLER_RECIPES
                .recipeBuilder(CosmicCore.id(recipeName(tier, input, 64)))
                .inputItems(transformer)
                .inputItems(source)
                .inputItems(GTCraftingComponents.WIRE_HEX.get(componentTier(tier)), 2)
                .inputItems(GTCraftingComponents.PLATE.get(componentTier(tier)), 6)
                .outputItems(result)
                .duration(400)
                .EUt(GTValues.VA[tier], 1)
                .addMaterialInfo(true)
                .save(provider);
    }

    private static void register256AEnergyHatch(RecipeOutput provider, int tier, boolean input) {
        MachineDefinition transformer = requireMachine(powerTransformer(tier), "power transformer", tier);
        MachineDefinition source = requireMachine(substationHatch(tier, input),
                input ? "64A substation energy hatch" : "64A substation dynamo hatch", tier);
        MachineDefinition result = requireMachine(
                input ? CosmicMachines.ENERGY_INPUT_HATCH_256A[tier] :
                        CosmicMachines.ENERGY_OUTPUT_HATCH_256A[tier],
                input ? "256A substation energy hatch" : "256A substation dynamo hatch", tier);

        ASSEMBLER_RECIPES
                .recipeBuilder(CosmicCore.id(recipeName(tier, input, 256)))
                .inputItems(transformer, 3)
                .inputItems(source)
                .inputItems(GTCraftingComponents.WIRE_HEX.get(componentTier(tier)), 4)
                .inputItems(GTCraftingComponents.PLATE.get(componentTier(tier)), 8)
                .outputItems(result)
                .duration(400)
                .EUt(GTValues.VA[tier], 4)
                .addMaterialInfo(true)
                .save(provider);
    }

    private static MachineDefinition substationHatch(int tier, boolean input) {
        if (tier <= HV) {
            return input ? CosmicMachines.ENERGY_INPUT_HATCH_64A[tier] :
                    CosmicMachines.ENERGY_OUTPUT_HATCH_64A[tier];
        }
        return input ? GTMachines.SUBSTATION_ENERGY_INPUT_HATCH[tier] :
                GTMachines.SUBSTATION_ENERGY_OUTPUT_HATCH[tier];
    }

    private static MachineDefinition powerTransformer(int tier) {
        int transformerTier = !GTCEuAPI.isHighTier() && tier == UHV ? UV : tier;
        return GTMachines.POWER_TRANSFORMER[transformerTier];
    }

    private static int componentTier(int tier) {
        return Math.min(tier, UHV);
    }

    private static String recipeName(int tier, boolean input, int amperage) {
        String direction = input ? "input" : "output";
        String voltage = GTValues.VN[tier].toLowerCase(Locale.ROOT);
        return "substation_" + direction + "_hatch_" + amperage + "a_" + voltage;
    }

    private static MachineDefinition requireMachine(MachineDefinition machine, String role, int tier) {
        return Objects.requireNonNull(machine, () -> "Missing " + GTValues.VN[tier] + " " + role);
    }
}
