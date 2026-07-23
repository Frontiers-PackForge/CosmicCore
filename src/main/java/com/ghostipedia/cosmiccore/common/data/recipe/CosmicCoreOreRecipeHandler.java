package com.ghostipedia.cosmiccore.common.data.recipe;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicBundleMaterials;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ghostipedia.cosmiccore.api.data.CosmicTagPrefix.*;
import static com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.crushed;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.crushedPurified;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dust;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.foil;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.gem;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.ingot;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.rawOre;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Water;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.CHEMICAL_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.FORGE_HAMMER_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.MACERATOR_RECIPES;

public class CosmicCoreOreRecipeHandler {

    private static final int REFINE_EUT = 8;

    public static void bundleInit(RecipeOutput provider, Material material) {
        List<Material> outputs = CosmicBundleMaterials.outputsOf(material);
        if (outputs == null || outputs.isEmpty()) return;
        entryStep(provider, material);
        refineChain(provider, material);
        for (CompositeOreSortingPlan.SortStage stage : CompositeOreSortingPlan.stages()) {
            sortAt(provider, stage, material, outputs);
        }
        furnaceBootstrap(provider, material);
    }

    private static void entryStep(RecipeOutput provider, Material material) {
        ItemStack crushedStack = ChemicalHelper.get(crushed, material);
        if (crushedStack.isEmpty()) return;
        FORGE_HAMMER_RECIPES.recipeBuilder("hammer_raw_" + material.getName() + "_to_crushed")
                .inputItems(rawOre, material)
                .outputItems(crushedStack.copyWithCount(1))
                .duration(10).EUt(16)
                .save(provider);
        MACERATOR_RECIPES.recipeBuilder("macerate_raw_" + material.getName() + "_to_crushed")
                .inputItems(rawOre, material)
                .outputItems(crushedStack.copyWithCount(2))
                .duration(400).EUt(2)
                .save(provider);
    }

    private static void refineChain(RecipeOutput provider, Material material) {
        floatStep(provider, material, crushed, crushedPurified, "float_purify_", 400, 500);
        millStep(provider, material, crushedPurified, powderizedOre, "mill_powder_", 600);
        flocculateStep(provider, material);
        floatStep(provider, material, flocculatedOre, crystallizedOreChunk, "crystallize_", 1600, 1000);
        floatStep(provider, material, crystallizedOreChunk, atomicallyPurifiedOreChunk, "atomically_purify_", 2600,
                1000);
    }

    private static void floatStep(RecipeOutput provider, Material material, TagPrefix in, TagPrefix out,
                                  String namePrefix, int duration, int water) {
        ItemStack outStack = ChemicalHelper.get(out, material);
        if (outStack.isEmpty()) return;
        INDUSTRIAL_FLOTATION_PLANT.recipeBuilder(namePrefix + material.getName())
                .inputItems(in, material)
                .inputFluids(Water.getFluid(water))
                .outputItems(outStack)
                .duration(duration).EUt(REFINE_EUT)
                .save(provider);
    }

    private static void flocculateStep(RecipeOutput provider, Material material) {
        ItemStack outStack = ChemicalHelper.get(flocculatedOre, material);
        if (outStack.isEmpty()) return;
        INDUSTRIAL_FLOTATION_PLANT.recipeBuilder("flocculate_" + material.getName())
                .inputItems(powderizedOre, material)
                .inputFluids(Water.getFluid(1000))
                .inputFluids(CosmicMaterials.PolyethyleneOxide.getFluid(10))
                .outputItems(outStack)
                .duration(1000).EUt(REFINE_EUT)
                .save(provider);
    }

    public static void registerFlocculant(RecipeOutput provider) {
        CHEMICAL_RECIPES.recipeBuilder("polyethylene_oxide_flocculant")
                .inputItems(foil, GTMaterials.Polyethylene)
                .inputFluids(GTMaterials.Oxygen.getFluid(1000))
                .outputFluids(CosmicMaterials.PolyethyleneOxide.getFluid(1000))
                .duration(120).EUt(GTValues.VA[GTValues.LV])
                .save(provider);
    }

    private static void millStep(RecipeOutput provider, Material material, TagPrefix in, TagPrefix out,
                                 String namePrefix, int duration) {
        ItemStack outStack = ChemicalHelper.get(out, material);
        if (outStack.isEmpty()) return;
        POWDERIZER.recipeBuilder(namePrefix + material.getName())
                .inputItems(in, material)
                .outputItems(outStack)
                .duration(duration).EUt(REFINE_EUT)
                .save(provider);
    }

    private static void sortAt(RecipeOutput provider, CompositeOreSortingPlan.SortStage stage, Material material,
                               List<Material> outputs) {
        int n = Math.min(Math.min(stage.typeCount(), outputs.size()), CompositeOreSortingPlan.SORTER_IO_CAP);
        if (n <= 0) return;
        var builder = INDUSTRIAL_ORE_SORTER.recipeBuilder(stage.recipeNamePrefix() + material.getName())
                .inputItems(stage.inputForm(), material);
        int emitted = 0;
        for (int i = 0; i < n; i++) {
            ItemStack chunk = chunkOf(outputs.get(i));
            if (chunk.isEmpty()) continue;
            builder.outputItems(chunk.copyWithCount(stage.outputAmount(i)));
            emitted++;
        }
        if (emitted == 0) return;
        builder.duration(CompositeOreSortingPlan.SORT_TIME_PER_TYPE * emitted)
                .EUt(CompositeOreSortingPlan.SORT_EUT).save(provider);
    }

    private static ItemStack chunkOf(Material mineral) {
        return ChemicalHelper.get(oreChunk, mineral);
    }

    public static void processChunkBasics(RecipeOutput provider, Material mineral) {
        if (ChemicalHelper.get(oreChunk, mineral).isEmpty()) return;
        ItemStack dustStack = ChemicalHelper.get(dust, mineral);
        if (dustStack.isEmpty()) return;
        MACERATOR_RECIPES.recipeBuilder("macerate_chunk_" + mineral.getName())
                .inputItems(oreChunk, mineral)
                .outputItems(dustStack)
                .duration(150).EUt(GTValues.VA[GTValues.LV])
                .save(provider);
    }

    private record FurnaceOut(TagPrefix prefix, Material material, int count) {}

    private static Map<Material, FurnaceOut> furnaceOutputs;

    private static Map<Material, FurnaceOut> furnaceOutputs() {
        if (furnaceOutputs == null) {
            Map<Material, FurnaceOut> map = new LinkedHashMap<>();
            map.put(CosmicBundleMaterials.Ferosine, new FurnaceOut(ingot, GTMaterials.Iron, 1));
            map.put(CosmicBundleMaterials.Cuprosiva, new FurnaceOut(ingot, GTMaterials.Copper, 1));
            map.put(CosmicBundleMaterials.Galenite, new FurnaceOut(ingot, GTMaterials.Lead, 1));
            map.put(CosmicBundleMaterials.Landisite, new FurnaceOut(ingot, GTMaterials.Nickel, 1));
            map.put(CosmicBundleMaterials.Redstona, new FurnaceOut(dust, GTMaterials.Redstone, 4));
            map.put(CosmicBundleMaterials.Lazuric, new FurnaceOut(gem, GTMaterials.Lapis, 4));
            map.put(CosmicBundleMaterials.Carbonic, new FurnaceOut(gem, GTMaterials.Coal, 4));
            map.put(CosmicBundleMaterials.EarthenSalts, new FurnaceOut(gem, GTMaterials.Salt, 4));
            map.put(CosmicBundleMaterials.Pyroltic, new FurnaceOut(dust, GTMaterials.Sulfur, 4));
            map.put(CosmicBundleMaterials.Quartizine, new FurnaceOut(gem, GTMaterials.CertusQuartz, 4));
            map.put(CosmicBundleMaterials.Molybite, new FurnaceOut(ingot, GTMaterials.Molybdenum, 1));
            map.put(CosmicBundleMaterials.Fahlorium, new FurnaceOut(ingot, GTMaterials.Copper, 1));
            map.put(CosmicBundleMaterials.MonaziteSalts, new FurnaceOut(dust, GTMaterials.Bastnasite, 4));
            furnaceOutputs = map;
        }
        return furnaceOutputs;
    }

    private static void furnaceBootstrap(RecipeOutput provider, Material material) {
        FurnaceOut out = furnaceOutputs().get(material);
        if (out == null) return;
        ItemStack raw = ChemicalHelper.get(rawOre, material);
        ItemStack result = ChemicalHelper.get(out.prefix(), out.material(), out.count());
        if (raw.isEmpty() || result.isEmpty()) return;
        VanillaRecipeHelper.addSmeltingRecipe(provider, CosmicCore.id("furnace_" + material.getName()),
                raw, result, 0.5f);
    }

    public static void disableBundleCauldronWash() {
        for (Material material : CosmicBundleMaterials.bundleOres()) {
            ItemStack crushedStack = ChemicalHelper.get(crushed, material);
            if (!crushedStack.isEmpty()) {
                CauldronInteraction.WATER.map().remove(crushedStack.getItem());
            }
        }
    }
}
