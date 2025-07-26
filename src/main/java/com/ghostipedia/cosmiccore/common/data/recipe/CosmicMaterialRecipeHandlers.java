package com.ghostipedia.cosmiccore.common.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static com.ghostipedia.cosmiccore.api.data.CosmicCustomTags.*;
import static com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes.HEAVY_ASSEMBLER;
import static com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes.ORBITAL_FORGE;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;

public class CosmicMaterialRecipeHandlers {

    public static void init(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        processHeavyBeam(provider, material);
        processModularShelling(provider, material);
        processUltraDensePlate(provider, material);
    }

    private static void processHeavyBeam(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(rod) || !material.shouldGenerateRecipesFor(plate) ||
                !material.hasProperty(PropertyKey.INGOT))
            return;
        HEAVY_ASSEMBLER.recipeBuilder("heavy_assemble_" + material.getName() + "_to_heavy_beam")
                .inputItems(plate, material, 4)
                .inputItems(plate, material, 4)
                .inputItems(plate, material, 4)
                .inputItems(plate, material, 4)
                .inputItems(rod, material, 16)
                .inputItems(rod, material, 16)
                .inputItems(rod, material, 16)
                .inputItems(rod, material, 16)
                .outputItems(heavyBeam, material, 1)
                .duration((int) material.getMass() * 64)
                .EUt(GTValues.VA[GTValues.LuV], 6)
                .circuitMeta(1)
                .save(provider);
    }

    private static void processModularShelling(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(plate) || !material.shouldGenerateRecipesFor(bolt) ||
                !material.shouldGenerateRecipesFor(frameGt) || !material.hasProperty(PropertyKey.INGOT))
            return;

        HEAVY_ASSEMBLER.recipeBuilder("heavy_assemble_" + material.getName() + "_modular_shelling")

                .inputItems(frameGt, material, 4)
                .outputItems(heavyBeam, 4)
                .inputItems(plate, material, 16)
                .inputItems(bolt, material, 32)
                .outputItems(modularShelling, material, 1)
                .duration((int) material.getMass())
                .EUt(GTValues.VA[GTValues.LuV], 6)
                .circuitMeta(3)
                .save(provider);
    }

    private static void processUltraDensePlate(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(plate) || !material.hasProperty(PropertyKey.INGOT)) return;

        ORBITAL_FORGE.recipeBuilder("heavy_forging" + material.getName() + "ultra_dense_plate")
                .inputItems(plate, material, 64)
                .outputItems(ultraDense, material, 1)
                .duration((int) material.getMass() * 15)
                .EUt(GTValues.VA[GTValues.LuV], 6)
                .blastFurnaceTemp(9500)
                .circuitMeta(7)
                .dimension(new ResourceLocation("frontiers:sun_orbit"))
                .save(provider);
    }
}
