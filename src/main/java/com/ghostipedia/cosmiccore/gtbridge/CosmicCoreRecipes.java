package com.ghostipedia.cosmiccore.gtbridge;

import com.ghostipedia.cosmiccore.api.capability.CosmicCapabilities;
import com.ghostipedia.cosmiccore.api.capability.recipe.CosmicRecipeCapabilities;
import com.ghostipedia.cosmiccore.api.capability.souls.SoulType;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulIngredient;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulStack;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.LarvaMachine;
import com.ghostipedia.cosmiccore.common.recipe.condition.LinkedPartnerCondition;
import com.ghostipedia.cosmiccore.common.recipe.condition.LinkedPartnerDimensionCondition;
import com.ghostipedia.cosmiccore.common.recipe.condition.LinkedPartnerDimensionFluidCondition;
import com.ghostipedia.cosmiccore.common.recipe.condition.LinkedPartnerDimensionItemCondition;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

import java.util.function.Consumer;

import static com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials.DilutedPrisma;
import static com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials.Prisma;
import static com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.DISTILLATION_RECIPES;

public class CosmicCoreRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        registerIndustrialPrimitiveBlastFurnaceRecipes(provider);

        DISTILLATION_RECIPES.recipeBuilder("diluted_prisma_to_prisma_and_water")
                .inputFluids(DilutedPrisma.getFluid(5000))
                .outputFluids(Prisma.getFluid(1000))
                .outputFluids(Water.getFluid(4000))
                .duration(40)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);

        LarvaMachine.generateTargettingChipRecipes(provider);

        // === Link Test Station Recipes ===
        // Basic recipe - no partner required (verifies machine works)
        LINK_TEST_RECIPES.recipeBuilder("link_test_basic")
                .inputItems(Items.IRON_INGOT)
                .outputItems(Items.IRON_NUGGET, 9)
                .duration(100)
                .EUt(GTValues.VA[GTValues.LV])
                .save(provider);

        // Linked recipe - requires at least 1 linked partner
        LINK_TEST_RECIPES.recipeBuilder("link_test_linked")
                .inputItems(Items.GOLD_INGOT)
                .outputItems(Items.DIAMOND)
                .duration(200)
                .EUt(GTValues.VA[GTValues.MV])
                .addCondition(new LinkedPartnerCondition(1))
                .save(provider);

        // Linked recipe - requires partner to be formed
        LINK_TEST_RECIPES.recipeBuilder("link_test_formed_partner")
                .inputItems(Items.EMERALD)
                .outputItems(Items.NETHER_STAR)
                .duration(400)
                .EUt(GTValues.VA[GTValues.HV])
                .addCondition(new LinkedPartnerCondition(1, true, false))
                .save(provider);

        // Linked recipe - requires partner in Moon dimension
        LINK_TEST_RECIPES.recipeBuilder("link_test_moon_partner")
                .inputItems(Items.LAPIS_LAZULI, 4)
                .outputItems(Items.ENDER_PEARL)
                .duration(200)
                .EUt(GTValues.VA[GTValues.MV])
                .addCondition(new LinkedPartnerDimensionCondition("ad_astra:moon"))
                .save(provider);

        // Linked recipe - requires partner in Overworld (for testing from other dimensions)
        LINK_TEST_RECIPES.recipeBuilder("link_test_overworld_partner")
                .inputItems(Items.REDSTONE, 4)
                .outputItems(Items.GLOWSTONE_DUST, 4)
                .duration(200)
                .EUt(GTValues.VA[GTValues.MV])
                .addCondition(new LinkedPartnerDimensionCondition("minecraft:overworld"))
                .save(provider);

        // Linked recipe - requires partner in Overworld with diamonds in input
        LINK_TEST_RECIPES.recipeBuilder("link_test_dimension_item")
                .inputItems(Items.COAL, 8)
                .outputItems(Items.DIAMOND)
                .duration(400)
                .EUt(GTValues.VA[GTValues.HV])
                .addCondition(new LinkedPartnerDimensionItemCondition("minecraft:overworld", Items.DIAMOND, 1))
                .save(provider);

        // Linked recipe - requires partner in Overworld with water in input
        LINK_TEST_RECIPES.recipeBuilder("link_test_dimension_fluid")
                .inputItems(Items.SPONGE)
                .outputItems(Items.WET_SPONGE)
                .duration(100)
                .EUt(GTValues.VA[GTValues.LV])
                .addCondition(new LinkedPartnerDimensionFluidCondition("minecraft:overworld", Fluids.WATER, 1000))
                .save(provider);

        /*
         * EMBER_TESTER_RECIPES.recipeBuilder("test")
         * .input(CosmicRecipeCapabilities.EMBER, 100d)
         * .outputItems(Items.COBBLESTONE)
         * .save(provider);
         */

        SOUL_TESTER_RECIPES.recipeBuilder("generate_soul")
                .notConsumable(Items.DIRT)
                .output(CosmicRecipeCapabilities.SOUL, SoulIngredient.of(SoulType.Raw, 10))
                .output(CosmicRecipeCapabilities.SOUL, SoulIngredient.of(SoulType.Temporal, 50))
                .duration(20)
                .save(provider);

        SOUL_TESTER_RECIPES.recipeBuilder("generate_soul_2")
                .inputItems(Items.DIRT)
                .input(CosmicRecipeCapabilities.SOUL, SoulIngredient.of(SoulType.Raw, 10))
                .outputItems(ingot, Steel)
                .duration(20)
                .save(provider);

        // GROVE_RECIPES.recipeBuilder("dirt_movement")
        // .input(SoulRecipeCapability.CAP, 100)
        // .notConsumable(CosmicItems.DONK)
        // .notConsumable(Items.ZOMBIE_HEAD)
        // .outputItems(Items.ROTTEN_FLESH, 1)
        // .duration(20)
        // .EUt(GTValues.VA[GTValues.HV])
        // .save(provider);
        // GROVE_RECIPES.recipeBuilder("killing_mobs")
        // .output(SoulRecipeCapability.CAP, 1000)
        // .notConsumable(Items.ZOMBIE_HEAD)
        // .duration(20)
        // .EUt(GTValues.VA[GTValues.HV])
        // .save(provider);
        // NAQUAHINE_REACTOR.recipeBuilder("dirt_to_power")
        // .inputItems(Blocks.DIRT.asItem(), 1)
        // .EUt(-GTValues.V[GTValues.UV])
        // .duration(10)
        // .save(provider);
    }

    private static void registerIndustrialPrimitiveBlastFurnaceRecipes(Consumer<FinishedRecipe> provider) {
        INDUSTRIAL_PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_coal_gem").inputItems(ingot, Iron)
                .inputItems(gem, Coal, 2).outputItems(ingot, Steel).outputItems(dustTiny, DarkAsh, 2)
                .duration((int) (1800 * 0.75f)).inputFluids(Creosote.getFluid(250))
                .save(provider);
        INDUSTRIAL_PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_coal_dust").inputItems(ingot, Iron)
                .inputItems(dust, Coal, 2).outputItems(ingot, Steel).outputItems(dustTiny, DarkAsh, 2)
                .duration((int) (1800 * 0.75f)).inputFluids(Creosote.getFluid(250))
                .save(provider);
        INDUSTRIAL_PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_charcoal_gem").inputItems(ingot, Iron)
                .inputItems(gem, Charcoal, 2).outputItems(ingot, Steel).outputItems(dustTiny, DarkAsh, 2)
                .duration((int) (1800 * 0.75f)).inputFluids(Creosote.getFluid(250))
                .save(provider);
        INDUSTRIAL_PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_charcoal_dust").inputItems(ingot, Iron)
                .inputItems(dust, Charcoal, 2).outputItems(ingot, Steel).outputItems(dustTiny, DarkAsh, 2)
                .inputFluids(Creosote.getFluid(250))
                .duration(1800).save(provider);
        INDUSTRIAL_PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_coke_gem").inputItems(ingot, Iron)
                .inputItems(gem, Coke).outputItems(ingot, Steel).chancedOutput(dust, Ash, "1/9", 0)
                .duration((int) (1500 * 0.75f)).inputFluids(Creosote.getFluid(250))
                .save(provider);
        INDUSTRIAL_PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_coke_dust").inputItems(ingot, Iron)
                .inputItems(dust, Coke).outputItems(ingot, Steel).chancedOutput(dust, Ash, "1/9", 0)
                .duration((int) (1500 * 0.75f)).inputFluids(Creosote.getFluid(250))
                .save(provider);

        INDUSTRIAL_PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_coal_block").inputItems(block, Iron)
                .inputItems(block, Coal, 2).outputItems(block, Steel).outputItems(dust, DarkAsh, 2)
                .duration((int) (16200 * 0.75f)).inputFluids(Creosote.getFluid(2000))
                .save(provider);
        INDUSTRIAL_PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_charcoal_block").inputItems(block, Iron)
                .inputItems(block, Charcoal, 2).outputItems(block, Steel).outputItems(dust, DarkAsh, 2)
                .duration((int) (16200 * 0.75f)).inputFluids(Creosote.getFluid(2000))
                .save(provider);
        INDUSTRIAL_PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_coke_block").inputItems(block, Iron)
                .inputItems(block, Coke).outputItems(block, Steel).outputItems(dust, Ash)
                .duration((int) (13500 * 0.75f)).inputFluids(Creosote.getFluid(2000))
                .save(provider);

        INDUSTRIAL_PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_coal_gem_wrought")
                .inputItems(ingot, WroughtIron)
                .inputItems(gem, Coal, 2).outputItems(ingot, Steel).outputItems(dustTiny, DarkAsh, 2)
                .duration((int) (800 * 0.75f)).inputFluids(Creosote.getFluid(250))
                .save(provider);
        INDUSTRIAL_PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_coal_dust_wrought")
                .inputItems(ingot, WroughtIron)
                .inputItems(dust, Coal, 2).outputItems(ingot, Steel).outputItems(dustTiny, DarkAsh, 2)
                .duration((int) (800 * 0.75f)).inputFluids(Creosote.getFluid(250))
                .save(provider);
        INDUSTRIAL_PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_charcoal_gem_wrought")
                .inputItems(ingot, WroughtIron)
                .inputItems(gem, Charcoal, 2).outputItems(ingot, Steel).outputItems(dustTiny, DarkAsh, 2)
                .duration((int) (800 * 0.75f)).inputFluids(Creosote.getFluid(250))
                .save(provider);
        INDUSTRIAL_PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_charcoal_dust_wrought")
                .inputItems(ingot, WroughtIron)
                .inputItems(dust, Charcoal, 2).outputItems(ingot, Steel).outputItems(dustTiny, DarkAsh, 2)
                .duration((int) (800 * 0.75f)).inputFluids(Creosote.getFluid(250))
                .save(provider);
        INDUSTRIAL_PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_coke_gem_wrought")
                .inputItems(ingot, WroughtIron)
                .inputItems(gem, Coke).outputItems(ingot, Steel).chancedOutput(dust, Ash, "1/9", 0)
                .duration((int) (600 * 0.75f)).inputFluids(Creosote.getFluid(250))
                .save(provider);
        INDUSTRIAL_PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_coke_dust_wrought")
                .inputItems(ingot, WroughtIron)
                .inputItems(dust, Coke).outputItems(ingot, Steel).chancedOutput(dust, Ash, "1/9", 0)
                .duration((int) (600 * 0.75f)).inputFluids(Creosote.getFluid(250))
                .save(provider);

        INDUSTRIAL_PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_coal_block_wrought")
                .inputItems(block, WroughtIron)
                .inputItems(block, Coal, 2).outputItems(block, Steel).outputItems(dust, DarkAsh, 2)
                .duration((int) (7200 * 0.75f)).inputFluids(Creosote.getFluid(2000))
                .save(provider);
        INDUSTRIAL_PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_charcoal_block_wrought")
                .inputItems(block, WroughtIron).inputItems(block, Charcoal, 2).outputItems(block, Steel)
                .outputItems(dust, DarkAsh, 2).duration((int) (7200 * 0.75f)).inputFluids(Creosote.getFluid(2000))
                .save(provider);
        INDUSTRIAL_PRIMITIVE_BLAST_FURNACE_RECIPES.recipeBuilder("steel_from_coke_block_wrought")
                .inputItems(block, WroughtIron)
                .inputItems(block, Coke).outputItems(block, Steel).outputItems(dust, Ash).duration((int) (5400 * 0.75f))
                .inputFluids(Creosote.getFluid(2000))
                .save(provider);
    }
}
