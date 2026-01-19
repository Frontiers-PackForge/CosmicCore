package com.ghostipedia.cosmiccore.gtbridge;

import com.ghostipedia.cosmiccore.api.capability.CosmicCapabilities;
import com.ghostipedia.cosmiccore.api.capability.recipe.CosmicRecipeCapabilities;
import com.ghostipedia.cosmiccore.api.capability.souls.SoulType;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulIngredient;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulStack;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.LarvaMachine;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;

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

        SOUL_TESTER_RECIPES.recipeBuilder("generate_soul")
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
