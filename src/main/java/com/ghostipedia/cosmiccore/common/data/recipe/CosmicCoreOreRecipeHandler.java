package com.ghostipedia.cosmiccore.common.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.OreProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.GTUtil;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

import static com.ghostipedia.cosmiccore.api.data.CosmicCoreTagPrefix.*;
import static com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes.*;
import static com.gregtechceu.gtceu.api.GTValues.L;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;


public class CosmicCoreOreRecipeHandler {
    public static void init(Consumer<FinishedRecipe> provider) {

        crushed.executeHandler(provider, PropertyKey.ORE, CosmicCoreOreRecipeHandler::processcrushedLeached);
    }

    public static void processcrushedLeached(TagPrefix crushedPrefix, Material material, OreProperty property,
                                             Consumer<FinishedRecipe> provider) {
        ItemStack leachedStack = ChemicalHelper.get(crushedLeached, material);

        Material byproduct = GTUtil.selectItemInList(
                0, material, property.getOreByProducts(), Material.class);
        Material byproduct2 = GTUtil.selectItemInList(
                Integer.MAX_VALUE, material, property.getOreByProducts(), Material.class);

        var builder = LEACHING_PLANT.recipeBuilder("crushed" + material.getName() + "_to_crushedleached")
                .inputItems(crushedPrefix, material)
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

}
