package com.ghostipedia.cosmiccore.integration.kjs.recipe;

import com.ghostipedia.cosmiccore.api.capability.recipe.SoulRecipeCapability;

import com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema;

import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import lombok.experimental.Accessors;

import static com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema.*;

public interface CosmicCoreRecipeSchema {

    @SuppressWarnings({ "unused", "UnusedReturnValue" })
    @Accessors(chain = true, fluent = true)
    class CosmicRecipeJS extends GTRecipeSchema.GTRecipeJS {

        public GTRecipeSchema.GTRecipeJS soulInput(int souls) {
            return this.input(SoulRecipeCapability.CAP, souls);
        }

        public GTRecipeSchema.GTRecipeJS soulOutput(int souls) {
            return this.output(SoulRecipeCapability.CAP, souls);
        }

        public GTRecipeSchema.GTRecipeJS magnetStats(int minField, int decayRate, boolean perTick) {
            this.addData("min_field", minField);
            this.addData("decay_rate", decayRate);
            this.addDataBool("per_tick", perTick);
            return this;
        }

        public GTRecipeSchema.GTRecipeJS magnetStats(int minField, int decayRate) {
            this.addData("min_field", minField);
            this.addData("decay_rate", decayRate);
            this.addDataBool("per_tick", true);
            return this;
        }
    }

    RecipeSchema SCHEMA = new RecipeSchema(CosmicRecipeJS.class, CosmicRecipeJS::new, DURATION, DATA, CONDITIONS,
            ALL_INPUTS, ALL_TICK_INPUTS, ALL_OUTPUTS, ALL_TICK_OUTPUTS,
            INPUT_CHANCE_LOGICS, OUTPUT_CHANCE_LOGICS, TICK_INPUT_CHANCE_LOGICS, TICK_OUTPUT_CHANCE_LOGICS,
            IS_FUEL, CATEGORY)
            .constructor((recipe, schemaType, keys, from) -> recipe.id(from.getValue(recipe, ID)), ID)
            .constructor(DURATION, CONDITIONS, ALL_INPUTS, ALL_OUTPUTS, ALL_TICK_INPUTS, ALL_TICK_OUTPUTS);
}
