package com.ghostipedia.cosmiccore.gtbridge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.HULL;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;
import static com.ghostipedia.cosmiccore.gtbridge.CosmicCoreRecipeTypes.ALTERNATOR_MACHINE_RECIPES;
import com.ghostipedia.cosmiccore.gtbridge.machine.kinetic.Alternator;
public class CosmicCoreRecipes {


    public static void init (Consumer<FinishedRecipe> provider) {
        Alternator(provider);
    }

    private static void Alternator(Consumer<FinishedRecipe> provider) {
    ALTERNATOR_MACHINE_RECIPES.recipeBuilder("SUtoEU")
            .rpm(32)
            .inputStress(1024)
            .EUt(-V[LV])
            .duration(10)
            .save(provider);
    }
}
