package com.ghostipedia.cosmiccore.mixin.gttweak;

import com.ghostipedia.cosmiccore.common.data.GTPetrochemicalRegistryKeys;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.common.data.GTRecipeCapabilities;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(value = GTRecipeBuilder.class, remap = false)
public abstract class GTPetrochemicalRecipeRetirementMixin {

    @Unique
    private static final Set<ResourceLocation> RETIRED_FLUIDS = cosmiccore$retiredFluids();
    @Unique
    private static final Set<ResourceLocation> RETIRED_OUTPUT_FLUIDS = Set.of(GTCEu.id("creosote"));
    @Unique
    private static final Set<ResourceLocation> RETIRED_RECIPE_IDS = cosmiccore$retiredRecipeIds();
    @Unique
    private static final Set<ResourceLocation> SOURCE_AUTHORITY_RETIRED_RECIPE_IDS = Set.of(
            GTCEu.id("centrifuge/brown_mushroom_separation"),
            GTCEu.id("centrifuge/endstone_separation"),
            GTCEu.id("centrifuge/nether_wart_separation"),
            GTCEu.id("centrifuge/red_mushroom_separation"),
            GTCEu.id("centrifuge/rubber_log_separation"),
            GTCEu.id("chemical_reactor/benzene_from_biphenyl"),
            GTCEu.id("chemical_reactor/methane_from_elements"),
            GTCEu.id("distillation_tower/distill_fermented_biomass"),
            GTCEu.id("electrolyzer/acetone_electrolysis"),
            GTCEu.id("electrolyzer/butane_electrolysis"),
            GTCEu.id("electrolyzer/butene_electrolysis"),
            GTCEu.id("extractor/monazite_extraction"),
            GTCEu.id("large_chemical_reactor/methane_shortcut"),
            GTCEu.id("large_chemical_reactor/phthalic_acid_from_naphthalene"),
            GTCEu.id("large_chemical_reactor/phthalic_acid_from_naphthalene_9"));

    @Inject(method = "save", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private void cosmiccore$retirePetrochemicalRecipe(RecipeOutput output, CallbackInfo ci) {
        GTRecipeBuilder builder = (GTRecipeBuilder) (Object) this;
        if (!GTCEu.MOD_ID.equals(builder.id.getNamespace())) return;
        ResourceLocation savedId = builder.recipeType == null ? builder.id :
                builder.id.withPrefix(builder.recipeType.registryName.getPath() + "/");
        if (SOURCE_AUTHORITY_RETIRED_RECIPE_IDS.contains(savedId) ||
                RETIRED_RECIPE_IDS.contains(builder.id) ||
                cosmiccore$containsFluid(builder.input, RETIRED_FLUIDS) ||
                cosmiccore$containsFluid(builder.tickInput, RETIRED_FLUIDS) ||
                cosmiccore$containsFluid(builder.output, RETIRED_FLUIDS) ||
                cosmiccore$containsFluid(builder.tickOutput, RETIRED_FLUIDS) ||
                cosmiccore$containsFluid(builder.output, RETIRED_OUTPUT_FLUIDS) ||
                cosmiccore$containsFluid(builder.tickOutput, RETIRED_OUTPUT_FLUIDS)) {
            ci.cancel();
        }
    }

    @Unique
    private static boolean cosmiccore$containsFluid(Map<?, List<Content>> contents, Set<ResourceLocation> retired) {
        List<Content> fluidContents = contents.get(GTRecipeCapabilities.FLUID);
        if (fluidContents == null) return false;
        for (Content content : fluidContents) {
            if (!(content.content() instanceof SizedFluidIngredient ingredient)) continue;
            for (var stack : ingredient.ingredient().getStacks()) {
                if (retired.contains(BuiltInRegistries.FLUID.getKey(stack.getFluid()))) return true;
            }
        }
        return false;
    }

    @Unique
    private static Set<ResourceLocation> cosmiccore$retiredFluids() {
        Set<ResourceLocation> retired = new HashSet<>();
        for (String path : List.of(
                "multi_phase_oil",
                "oil",
                "light_oil",
                "heavy_oil",
                "natural_gas",
                "sour_refinery_gas",
                "sour_naphtha",
                "sour_middle_fraction_distillates",
                "sour_gas_oils",
                "refinery_gas",
                "light_naphtha",
                "middle_fraction_distillates",
                "gas_oils",
                "mixed_xylenes",
                "charcoal_byproducts",
                "wood_gas",
                "wood_vinegar",
                "wood_tar",
                "raw_coking_gas",
                "coal_tar")) {
            retired.add(GTPetrochemicalRegistryKeys.canonicalId(path));
        }
        for (String feed : List.of("ethane", "ethylene", "propene", "propane", "butane", "butene", "butadiene")) {
            retired.add(GTCEu.id("hydro_cracked_" + feed));
            retired.add(GTCEu.id("steam_cracked_" + feed));
        }
        for (String cut : List.of("heavy_fuel", "light_fuel", "naphtha", "gas")) {
            retired.add(GTCEu.id("lightly_hydro_cracked_" + cut));
            retired.add(GTCEu.id("severely_hydro_cracked_" + cut));
            retired.add(GTCEu.id("lightly_steam_cracked_" + cut));
            retired.add(GTCEu.id("severely_steam_cracked_" + cut));
        }
        return Set.copyOf(retired);
    }

    @Unique
    private static Set<ResourceLocation> cosmiccore$retiredRecipeIds() {
        Set<ResourceLocation> retired = new HashSet<>();
        for (String path : List.of(
                "oilsands_ore_separation",
                "oilsands_dust_separation",
                "ethylene_from_ethanol",
                "distill_oil",
                "distill_light_oil",
                "distill_heavy_oil",
                "distill_raw_oil",
                "distill_refinery_gas",
                "distill_creosote",
                "distill_charcoal_byproducts",
                "distill_wood_tar",
                "distill_wood_vinegar",
                "distill_wood_gas",
                "distill_coal_gas",
                "distill_coal_tar")) {
            retired.add(GTCEu.id(path));
        }
        for (String feed : List.of("ethane", "ethylene", "propene", "propane", "butane", "butene", "butadiene")) {
            retired.add(GTCEu.id("distill_hydro_cracked_" + feed));
            retired.add(GTCEu.id("distill_steam_cracked_" + feed));
        }
        for (String cut : List.of("heavy_fuel", "light_fuel", "naphtha", "gas")) {
            retired.add(GTCEu.id("distill_lightly_hydro_cracked_" + cut));
            retired.add(GTCEu.id("distill_severely_hydro_cracked_" + cut));
            retired.add(GTCEu.id("distill_lightly_steam_cracked_" + cut));
            retired.add(GTCEu.id("distill_severely_steam_cracked_" + cut));
        }
        return Set.copyOf(retired);
    }
}
