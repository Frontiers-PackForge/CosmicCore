package com.ghostipedia.cosmiccore.gtbridge;

import net.minecraft.data.recipes.FinishedRecipe;
import java.util.function.Consumer;

import static com.ghostipedia.cosmiccore.gtbridge.CosmicCoreRecipeTypes.*;
import static com.gregtechceu.gtceu.api.GTValues.*;

@SuppressWarnings("unused")
public class CosmicCoreRecipes {
    private static int dur(int seconds) {
        return seconds * 20;
    }

    public static void init(Consumer<FinishedRecipe> provider) {
        AltGen(provider);
    }

    private static void AltGen(Consumer<FinishedRecipe> provider) {


        ALTERNATOR_MACHINE_RECIPES.recipeBuilder("generatorrecipe")
                //.addCondition()
                .inputStress(256)
                .rpm(32)
                .EUt(-32)
                .duration(16).EUt(VA[MV]).save(provider);
    }
}
