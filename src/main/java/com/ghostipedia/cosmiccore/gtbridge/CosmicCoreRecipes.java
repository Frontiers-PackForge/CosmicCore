package com.ghostipedia.cosmiccore.gtbridge;


import com.ghostipedia.cosmiccore.api.capability.recipe.SoulRecipeCapability;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.data.recipe.serialized.chemistry.DistillationRecipes;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

import static com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials.DilutedPrisma;
import static com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials.Prisma;
import static com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Water;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.DISTILLATION_RECIPES;

public class CosmicCoreRecipes {


    public static void init(Consumer<FinishedRecipe> provider) {
//        SOUL_TESTER_RECIPES.recipeBuilder("dirt_to_soul")
//                .inputItems(Blocks.DIRT.asItem(), 1)
//                .output(SoulRecipeCapability.CAP, 1000000)
//                .duration(10)
//                .save(provider);
//

        DISTILLATION_RECIPES.recipeBuilder("diluted_prisma_to_prisma_and_water")
                .inputFluids(DilutedPrisma.getFluid(5000))
                .outputFluids(Prisma.getFluid(1000))
                .outputFluids(Water.getFluid(4000))
                .duration(40)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);
//        GROVE_RECIPES.recipeBuilder("dirt_movement")
//                .input(SoulRecipeCapability.CAP, 100)
//                .notConsumable(CosmicItems.DONK)
//                .notConsumable(Items.ZOMBIE_HEAD)
//                .outputItems(Items.ROTTEN_FLESH, 1)
//                .duration(20)
//                .EUt(GTValues.VA[GTValues.HV])
//                .save(provider);
//        GROVE_RECIPES.recipeBuilder("killing_mobs")
//                .output(SoulRecipeCapability.CAP, 1000)
//                .notConsumable(Items.ZOMBIE_HEAD)
//                .duration(20)
//                .EUt(GTValues.VA[GTValues.HV])
//                .save(provider);
//        NAQUAHINE_REACTOR.recipeBuilder("dirt_to_power")
//                .inputItems(Blocks.DIRT.asItem(), 1)
//                .EUt(-GTValues.V[GTValues.UV])
//                .duration(10)
//                .save(provider);
    }

}
