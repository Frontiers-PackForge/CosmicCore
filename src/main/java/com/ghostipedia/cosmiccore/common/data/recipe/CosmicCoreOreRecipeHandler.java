package com.ghostipedia.cosmiccore.common.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.OreProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

import static com.ghostipedia.cosmiccore.api.data.CosmicCustomTags.*;
import static com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials.DilutedPrisma;
import static com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials.Prisma;
import static com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class CosmicCoreOreRecipeHandler {

    public static void init(Consumer<FinishedRecipe> provider) {
        crushed.executeHandler(provider, PropertyKey.ORE, CosmicCoreOreRecipeHandler::processcrushedLeached);
        crushedRefined.executeHandler(provider, PropertyKey.ORE, CosmicCoreOreRecipeHandler::processRefinedFrothed);
        crushedLeached.executeHandler(provider, PropertyKey.ORE, CosmicCoreOreRecipeHandler::processLeachedRefined);
        prismaFrothed.executeHandler(provider, PropertyKey.ORE, CosmicCoreOreRecipeHandler::processFrothedPure);
    }

    public static void processcrushedLeached(TagPrefix crushedPrefix, Material material, OreProperty property,
                                             Consumer<FinishedRecipe> provider) {
        ItemStack leachedStack = ChemicalHelper.get(crushedLeached, material);

        Material byproduct = GTUtil.selectItemInList(
                0, material, property.getOreByProducts(), Material.class);
        Material byproduct2 = GTUtil.selectItemInList(
                Integer.MAX_VALUE, material, property.getOreByProducts(), Material.class);

        var builder = LEACHING_PLANT.recipeBuilder("crushed" + material.getName() + "_to_crushedleached")
                .inputItems(crushedPurified, material)
                .inputFluids(Water.getFluid(100))
                .inputFluids(SulfuricAcid.getFluid(200))
                .outputItems(leachedStack)
                .chancedOutput(leachedStack, 5500, 750);
        if (byproduct != null && !ChemicalHelper.get(dustPure, byproduct).isEmpty()) {
            builder.chancedOutput(dustPure, byproduct, 1500, 1350);
        }
        if (byproduct2 != null && !ChemicalHelper.get(dustPure, byproduct2).isEmpty()) {
            builder.chancedOutput(dustPure, byproduct2, 2200, 1150);
        }
        builder.outputFluids(DilutedSulfuricAcid.getFluid(300));
        builder.duration(60).EUt(GTValues.VA[GTValues.HV]).save(provider);
    }

    public static void processRefinedFrothed(TagPrefix refinedPrefix, Material material, OreProperty property,
                                             Consumer<FinishedRecipe> provider) {
        ItemStack frothedStack = ChemicalHelper.get(prismaFrothed, material);

        Material byproduct = GTUtil.selectItemInList(
                0, material, property.getOreByProducts(), Material.class);
        Material byproduct2 = GTUtil.selectItemInList(
                1, material, property.getOreByProducts(), Material.class);
        Material byproduct3 = GTUtil.selectItemInList(
                2, material, property.getOreByProducts(), Material.class);
        Material byproduct4 = GTUtil.selectItemInList(
                Integer.MAX_VALUE, material, property.getOreByProducts(), Material.class);

        var builder = CHROMATIC_FLOTATION_PLANT.recipeBuilder("refined" + material.getName() + "_to_frothed")
                .inputItems(refinedPrefix, material)
                .inputFluids(Prisma.getFluid(1000))
                .outputItems(GTUtil.copyAmount(2, frothedStack));
        if (byproduct != null && !ChemicalHelper.get(dustImpure, byproduct).isEmpty()) {
            builder.chancedOutput(dustImpure, byproduct, 3500, 1450);
        }
        if (byproduct2 != null && !ChemicalHelper.get(dustImpure, byproduct2).isEmpty()) {
            builder.chancedOutput(dustImpure, byproduct2, 1800, 1750);
        }
        if (byproduct3 != null && !ChemicalHelper.get(dustPure, byproduct3).isEmpty()) {
            builder.chancedOutput(dustImpure, byproduct3, 1500, 1950);
        }
        if (byproduct4 != null && !ChemicalHelper.get(dustPure, byproduct4).isEmpty()) {
            builder.chancedOutput(dustImpure, byproduct4, 1500, 1950);
        }
        builder.outputFluids(Prisma.getFluid(500));
        builder.duration(40).EUt(GTValues.VA[GTValues.IV]).save(provider);
    }

    public static void processLeachedRefined(TagPrefix leachedPrefix, Material material, OreProperty property,
                                             Consumer<FinishedRecipe> provider) {
        ItemStack refinedStack = ChemicalHelper.get(crushedRefined, material);

        Material byproduct = GTUtil.selectItemInList(
                1, material, property.getOreByProducts(), Material.class);

        var builder = THERMAL_CENTRIFUGE_RECIPES.recipeBuilder("leached" + material.getName() + "_to_refined")
                .inputItems(leachedPrefix, material)
                .outputItems(refinedStack);
        if (byproduct != null && !ChemicalHelper.get(dust, byproduct).isEmpty()) {
            builder.chancedOutput(dust, byproduct, 2500, 1000);
        }
        builder.duration(40).EUt(GTValues.VA[GTValues.HV]).save(provider);
    }

    public static void processFrothedPure(TagPrefix frothedPrefix, Material material, OreProperty property,
                                          Consumer<FinishedRecipe> provider) {
        ItemStack pureStack = ChemicalHelper.get(dustPure, material);

        Material byproduct = GTUtil.selectItemInList(
                Integer.MAX_VALUE, material, property.getOreByProducts(), Material.class);

        var builder = CHEMICAL_BATH_RECIPES.recipeBuilder("frothed" + material.getName() + "_to_purified")
                .inputItems(frothedPrefix, material)
                .inputFluids(Water.getFluid(1000))
                .outputItems(pureStack);
        if (byproduct != null && !ChemicalHelper.get(dustPure, byproduct).isEmpty()) {
            builder.chancedOutput(dustPure, byproduct, 1500, 1950);
        }
        builder.outputFluids(DilutedPrisma.getFluid(1250));
        builder.duration(40).EUt(GTValues.VA[GTValues.IV]).save(provider);
    }
}
