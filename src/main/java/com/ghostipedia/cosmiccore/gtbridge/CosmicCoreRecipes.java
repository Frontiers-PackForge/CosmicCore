package com.ghostipedia.cosmiccore.gtbridge;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.data.recipes.FinishedRecipe;

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
