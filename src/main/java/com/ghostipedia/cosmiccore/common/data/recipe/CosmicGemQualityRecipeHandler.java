package com.ghostipedia.cosmiccore.common.data.recipe;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.EXPLOSIVE;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.FLAMMABLE;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.gem;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.gemFlawless;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.IMPLOSION_RECIPES;

public final class CosmicGemQualityRecipeHandler {

    private CosmicGemQualityRecipeHandler() {}

    public static void register(RecipeOutput provider) {
        for (Material material : GTRegistries.MATERIALS) {
            registerMaterial(provider, material);
        }
    }

    private static void registerMaterial(RecipeOutput provider, Material material) {
        if (!material.hasProperty(PropertyKey.GEM) ||
                material.hasAnyOfFlags(EXPLOSIVE, FLAMMABLE) ||
                !material.shouldGenerateRecipesFor(gem) ||
                !material.shouldGenerateRecipesFor(gemFlawless)) {
            return;
        }

        ItemStack regularGems = ChemicalHelper.get(gem, material, 4);
        ItemStack flawlessGem = ChemicalHelper.get(gemFlawless, material);
        if (regularGems.isEmpty() || flawlessGem.isEmpty()) {
            return;
        }

        ResourceLocation materialId = material.getResourceLocation();
        String recipePath = "implosion/gem_quality/" + materialId.getNamespace() + "/" + materialId.getPath();

        IMPLOSION_RECIPES.recipeBuilder(CosmicCore.id(recipePath + "/powder_barrel"))
                .inputItems(regularGems.copy())
                .explosivesType(GTBlocks.POWDERBARREL.asStack(8))
                .outputItems(flawlessGem.copy())
                .save(provider);
        IMPLOSION_RECIPES.recipeBuilder(CosmicCore.id(recipePath + "/tnt"))
                .inputItems(regularGems.copy())
                .explosivesAmount(4)
                .outputItems(flawlessGem.copy())
                .save(provider);
        IMPLOSION_RECIPES.recipeBuilder(CosmicCore.id(recipePath + "/dynamite"))
                .inputItems(regularGems.copy())
                .explosivesType(GTItems.DYNAMITE.asStack(2))
                .outputItems(flawlessGem.copy())
                .save(provider);
        IMPLOSION_RECIPES.recipeBuilder(CosmicCore.id(recipePath + "/industrial_tnt"))
                .inputItems(regularGems.copy())
                .explosivesType(GTBlocks.INDUSTRIAL_TNT.asStack())
                .outputItems(flawlessGem.copy())
                .save(provider);
    }
}
