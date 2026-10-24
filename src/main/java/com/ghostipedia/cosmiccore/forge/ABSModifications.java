package com.ghostipedia.cosmiccore.forge;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.AlloyBlastProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.misc.alloyblast.CustomAlloyBlastRecipeProducer;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ABSModifications {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void addAlloyBlastProperties(PostMaterialEvent event) {
        List<String> restoredMaterials = new ArrayList<>();
        List<String> cappedTemperatures = new ArrayList<>();
        for (Material material : GTRegistries.MATERIALS) {
            int originalTemperature = material.getBlastTemperature();
            applyCasingTemperature(material);
            if (material.getBlastTemperature() != originalTemperature) {
                cappedTemperatures.add(material.getResourceLocation() + ": " + originalTemperature + " -> " +
                        material.getBlastTemperature() + " K");
            }
            if (!material.hasFlag(MaterialFlags.DISABLE_ALLOY_PROPERTY) && addAlloyBlastProperty(material)) {
                restoredMaterials.add(material.getResourceLocation().toString());
            }
        }

        cappedTemperatures.sort(Comparator.naturalOrder());
        CosmicCore.LOGGER.info("Applied GCYM casing alloy heat limits: {}", cappedTemperatures);

        restoredMaterials.sort(Comparator.naturalOrder());
        if (restoredMaterials.size() > AlloyBlastLifecyclePolicy.MAX_RESTORED_ALLOYS) {
            throw new IllegalStateException("Refusing an unbounded alloy blast restoration of " +
                    restoredMaterials.size() + " materials");
        }

        GTMaterials.NiobiumNitride.getProperty(PropertyKey.ALLOY_BLAST)
                .setRecipeProducer(new CustomAlloyBlastRecipeProducer(1, 11, -1));
        GTMaterials.IndiumTinBariumTitaniumCuprate.getProperty(PropertyKey.ALLOY_BLAST)
                .setRecipeProducer(new CustomAlloyBlastRecipeProducer(-1, -1, 16));

        Material material = CosmicMaterials.ResonantVirtueMeld.value();
        AlloyBlastProperty property = material.getProperty(PropertyKey.ALLOY_BLAST);
        property.setRecipeProducer(new CustomAlloyBlastRecipeProducer(-1, -1, 32));

        Material hslaSteel = GTMaterials.HSLASteel.value();
        AlloyBlastLifecyclePolicy.RecipeShape hslaShape = recipeShape(hslaSteel);
        if (!canGenerateAlloyBlastRecipe(hslaSteel) ||
                hslaSteel.getFluidBuilder(FluidStorageKeys.LIQUID) == null ||
                hslaShape.itemInputs() != 4 || hslaShape.fluidInputs() != 0 ||
                hslaShape.outputUnits() != 5 || hslaShape.circuitMeta() != 4) {
            throw new IllegalStateException("HSLA Steel alloy blast lifecycle is incomplete: " + hslaShape);
        }

        int generatedRecipes = 0;
        for (Material candidate : GTRegistries.MATERIALS) {
            if (!canGenerateAlloyBlastRecipe(candidate)) continue;
            generatedRecipes++;
            if (candidate.getProperty(PropertyKey.BLAST).getGasTier() != null) generatedRecipes++;
        }
        CosmicCore.LOGGER.info(
                "Restored {} GTM alloy blast material properties {}; projected {} generated Alloy Blasting Kiln recipes using GTM liquid casting; HSLA Steel and Watertight Steel enabled: {}, {}",
                restoredMaterials.size(), restoredMaterials, generatedRecipes,
                canGenerateAlloyBlastRecipe(hslaSteel),
                canGenerateAlloyBlastRecipe(GTMaterials.WatertightSteel.value()));
    }

    private static boolean addAlloyBlastProperty(Material material) {
        if (material.getMaterialComponents().size() < 2 ||
                !material.hasProperty(PropertyKey.BLAST) ||
                !material.hasProperty(PropertyKey.FLUID) ||
                material.hasProperty(PropertyKey.ALLOY_BLAST) ||
                material.getMaterialComponents().stream().filter(ABSModifications::isFluidOnly).limit(3).count() > 2) {
            return false;
        }
        material.setProperty(PropertyKey.ALLOY_BLAST, new AlloyBlastProperty());
        return true;
    }

    private static void applyCasingTemperature(Material material) {
        if (!material.getResourceLocation().getNamespace().equals("gtceu") ||
                !material.hasProperty(PropertyKey.BLAST))
            return;
        Integer temperature = AlloyBlastLifecyclePolicy.casingTemperatureOverride(
                material.getName(), material.getBlastTemperature());
        if (temperature == null) return;
        material.getProperty(PropertyKey.BLAST).setBlastTemperature(temperature);
        FluidBuilder liquidBuilder = material.getFluidBuilder(FluidStorageKeys.LIQUID);
        if (liquidBuilder != null) liquidBuilder.temperature(temperature);
    }

    private static boolean isFluidOnly(MaterialStack stack) {
        return !stack.material().hasProperty(PropertyKey.DUST) &&
                stack.material().hasProperty(PropertyKey.FLUID);
    }

    private static boolean canGenerateAlloyBlastRecipe(Material material) {
        if (!TagPrefix.dust.doGenerateItem(material) ||
                !material.hasProperty(PropertyKey.DUST) ||
                !material.hasProperty(PropertyKey.INGOT) ||
                !material.hasProperty(PropertyKey.ALLOY_BLAST) ||
                material.hasAnyOfFlags(MaterialFlags.FLAMMABLE, MaterialFlags.NO_SMELTING,
                        MaterialFlags.DISABLE_ALLOY_BLAST, MaterialFlags.DISABLE_MATERIAL_RECIPES)) {
            return false;
        }
        return recipeShape(material).valid();
    }

    private static AlloyBlastLifecyclePolicy.RecipeShape recipeShape(Material material) {
        return AlloyBlastLifecyclePolicy.recipeShape(material.getMaterialComponents().stream()
                .map(stack -> new AlloyBlastLifecyclePolicy.Component(inputForm(stack), (int) stack.amount()))
                .toList());
    }

    private static AlloyBlastLifecyclePolicy.InputForm inputForm(MaterialStack stack) {
        if (stack.material().hasProperty(PropertyKey.DUST)) return AlloyBlastLifecyclePolicy.InputForm.DUST;
        if (stack.material().hasProperty(PropertyKey.FLUID)) return AlloyBlastLifecyclePolicy.InputForm.FLUID;
        return AlloyBlastLifecyclePolicy.InputForm.INVALID;
    }
}
