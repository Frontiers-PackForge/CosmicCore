package com.ghostipedia.cosmiccore.common.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static com.ghostipedia.cosmiccore.api.data.CosmicCustomTags.*;
import static com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials.DilutedPrisma;
import static com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials.Prisma;
import static com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class CosmicCoreOreRecipeHandler {

    public static void init(Consumer<FinishedRecipe> provider, @NotNull Material material) {
        processcrushedLeached(provider, material);
        processRefinedFrothed(provider, material);
        processLeachedRefined(provider, material);
        processFrothedPure(provider, material);
        processRawOretoFinalStates(provider, material);
        // todo old
        // crushed.executeHandler(provider, PropertyKey.ORE, CosmicCoreOreRecipeHandler::processcrushedLeached);
        // crushedRefined.executeHandler(provider, PropertyKey.ORE, CosmicCoreOreRecipeHandler::processRefinedFrothed);
        // crushedLeached.executeHandler(provider, PropertyKey.ORE, CosmicCoreOreRecipeHandler::processLeachedRefined);
        // prismaFrothed.executeHandler(provider, PropertyKey.ORE, CosmicCoreOreRecipeHandler::processFrothedPure);
    }

    public static void processcrushedLeached(Consumer<FinishedRecipe> provider, Material material) {
        if (!material.shouldGenerateRecipesFor(crushed) || !material.hasProperty(PropertyKey.ORE)) return;
        var property = material.getProperty(PropertyKey.ORE);
        ItemStack leachedStack = ChemicalHelper.get(crushedLeached, material);

        Material byproduct = property.getOreByProduct(0);
        Material byproduct2 = property.getOreByProduct(1);

        var builder = LEACHING_PLANT.recipeBuilder("crushed" + material.getName() + "_to_crushedleached")
                .inputItems(crushedPurified, material)
                .inputFluids(Water.getFluid(100))
                .inputFluids(SulfuricAcid.getFluid(200))
                .outputItems(leachedStack)
                .chancedOutput(leachedStack, 5500, 750);
        if (byproduct != GTMaterials.NULL && !ChemicalHelper.get(dustPure, byproduct).isEmpty()) {
            builder.chancedOutput(dustPure, byproduct, 1500, 1350);
        }
        if (byproduct2 != GTMaterials.NULL && !ChemicalHelper.get(dustPure, byproduct2).isEmpty()) {
            builder.chancedOutput(dustPure, byproduct2, 2200, 1150);
        }
        builder.outputFluids(DilutedSulfuricAcid.getFluid(300));
        builder.duration(60).EUt(GTValues.VA[GTValues.HV]).save(provider);
    }

    public static void processRefinedFrothed(Consumer<FinishedRecipe> provider, Material material) {
        if (!material.shouldGenerateRecipesFor(crushedRefined) || !material.hasProperty(PropertyKey.ORE)) return;
        var property = material.getProperty(PropertyKey.ORE);
        ItemStack frothedStack = ChemicalHelper.get(prismaFrothed, material);

        Material byproduct = property.getOreByProduct(0);
        Material byproduct2 = property.getOreByProduct(1);
        Material byproduct3 = property.getOreByProduct(2);
        Material byproduct4 = property.getOreByProduct(Integer.MAX_VALUE);

        var builder = CHROMATIC_FLOTATION_PLANT.recipeBuilder("refined" + material.getName() + "_to_frothed")
                .inputItems(crushedRefined, material)
                .inputFluids(Prisma.getFluid(1000))
                .outputItems(frothedStack.copyWithCount(2));
        if (byproduct != GTMaterials.NULL && !ChemicalHelper.get(dustImpure, byproduct).isEmpty()) {
            builder.chancedOutput(dustImpure, byproduct, 3500, 1450);
        }
        if (byproduct2 != GTMaterials.NULL && !ChemicalHelper.get(dustImpure, byproduct2).isEmpty()) {
            builder.chancedOutput(dustImpure, byproduct2, 1800, 1750);
        }
        if (byproduct3 != GTMaterials.NULL && !ChemicalHelper.get(dustPure, byproduct3).isEmpty()) {
            builder.chancedOutput(dustImpure, byproduct3, 1500, 1950);
        }
        if (byproduct4 != GTMaterials.NULL && !ChemicalHelper.get(dustPure, byproduct4).isEmpty()) {
            builder.chancedOutput(dustImpure, byproduct4, 1500, 1950);
        }
        builder.outputFluids(Prisma.getFluid(500));
        builder.duration(40).EUt(GTValues.VA[GTValues.IV]).save(provider);
    }

    public static void processLeachedRefined(Consumer<FinishedRecipe> provider, Material material) {
        if (!material.shouldGenerateRecipesFor(crushedLeached) || !material.hasProperty(PropertyKey.ORE)) return;
        ItemStack refinedStack = ChemicalHelper.get(crushedRefined, material);
        var property = material.getProperty(PropertyKey.ORE);

        Material byproduct = property.getOreByProduct(1);

        var builder = THERMAL_CENTRIFUGE_RECIPES.recipeBuilder("leached" + material.getName() + "_to_refined")
                .inputItems(crushedLeached, material)
                .outputItems(refinedStack);
        if (byproduct != GTMaterials.NULL && !ChemicalHelper.get(dust, byproduct).isEmpty()) {
            builder.chancedOutput(dust, byproduct, 2500, 1000);
        }
        builder.duration(40).EUt(GTValues.VA[GTValues.HV]).save(provider);
    }

    public static void processFrothedPure(Consumer<FinishedRecipe> provider, Material material) {
        if (!material.shouldGenerateRecipesFor(prismaFrothed) || !material.hasProperty(PropertyKey.ORE)) return;
        ItemStack refinedStack = ChemicalHelper.get(prismaFrothed, material);
        ItemStack pureStack = ChemicalHelper.get(dustPure, material);
        var property = material.getProperty(PropertyKey.ORE);

        Material byproduct = property.getOreByProduct(0);

        var builder = CHEMICAL_BATH_RECIPES.recipeBuilder("frothed" + material.getName() + "_to_purified")
                .inputItems(prismaFrothed, material)
                .inputFluids(Water.getFluid(1000))
                .outputItems(pureStack);
        if (byproduct != GTMaterials.NULL && !ChemicalHelper.get(dustPure, byproduct).isEmpty()) {
            builder.chancedOutput(dustPure, byproduct, 1500, 1950);
        }
        builder.outputFluids(DilutedPrisma.getFluid(1250));
        builder.duration(40).EUt(GTValues.VA[GTValues.IV]).save(provider);
    }

    // Prismatic Foundry

    public static void processRawOretoFinalStates(Consumer<FinishedRecipe> provider, Material material) {
        if (!material.shouldGenerateRecipesFor(rawOre) || !material.hasProperty(PropertyKey.ORE)) return;
        var property = material.getProperty(PropertyKey.ORE);
        ItemStack frothedStack = ChemicalHelper.get(dust, material);

        Material byproduct = property.getOreByProduct(0);
        Material byproduct2 = property.getOreByProduct(1);
        Material byproduct3 = property.getOreByProduct(2);
        Material byproduct4 = property.getOreByProduct(Integer.MAX_VALUE);

        var builder = PRISMA_FOUNDRY.recipeBuilder("raw_ore_prismf_" + material.getName() + "_to_dusts")
                .inputItems(rawOre, material)
                .inputFluids(GTMaterials.Blaze.getFluid(250))
                .inputFluids(Water.getFluid(2750))
                .outputItems(frothedStack.copyWithCount(6));
        if (byproduct != GTMaterials.NULL && !ChemicalHelper.get(dust, byproduct).isEmpty()) {
            builder.chancedOutput(dust, byproduct, 3500, 0);
        }
        if (byproduct2 != GTMaterials.NULL && !ChemicalHelper.get(dust, byproduct2).isEmpty()) {
            builder.chancedOutput(dust, byproduct2, 3500, 0);
        }
        if (byproduct3 != GTMaterials.NULL && !ChemicalHelper.get(dust, byproduct3).isEmpty()) {
            builder.chancedOutput(dust, byproduct3, 3500, 0);
        }
        if (byproduct4 != GTMaterials.NULL && !ChemicalHelper.get(dust, byproduct4).isEmpty()) {
            builder.chancedOutput(dust, byproduct4, 3500, 0);
        }
        builder.duration(40).EUt(GTValues.V[GTValues.IV]).save(provider);
    }
}
