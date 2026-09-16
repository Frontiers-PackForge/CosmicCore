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
import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.misc.alloyblast.CustomAlloyBlastRecipeProducer;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ABSModifications {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void addAlloyBlastProperties(PostMaterialEvent event) {
        int restored = 0;
        for (Material material : GTRegistries.MATERIALS) {
            if (!material.hasFlag(MaterialFlags.DISABLE_ALLOY_PROPERTY) && addAlloyBlastProperty(material)) {
                restored++;
            }
        }

        GTMaterials.NiobiumNitride.getProperty(PropertyKey.ALLOY_BLAST)
                .setRecipeProducer(new CustomAlloyBlastRecipeProducer(1, 11, -1));
        GTMaterials.IndiumTinBariumTitaniumCuprate.getProperty(PropertyKey.ALLOY_BLAST)
                .setRecipeProducer(new CustomAlloyBlastRecipeProducer(-1, -1, 16));

        Material material = CosmicMaterials.ResonantVirtueMeld;
        AlloyBlastProperty property = material.getProperty(PropertyKey.ALLOY_BLAST);
        property.setRecipeProducer(new CustomAlloyBlastRecipeProducer(-1, -1, 32));
        int generatedRecipes = 0;
        int freezerRecipes = 0;
        for (Material candidate : GTRegistries.MATERIALS) {
            if (!canGenerateAlloyBlastRecipe(candidate)) continue;
            generatedRecipes++;
            if (candidate.getProperty(PropertyKey.BLAST).getGasTier() != null) generatedRecipes++;
            if (TagPrefix.ingotHot.doGenerateItem(candidate)) freezerRecipes++;
        }
        CosmicCore.LOGGER.info(
                "Restored {} GTM alloy blast material properties; projected {} generated alloy blast recipes and {} molten-to-ingot vacuum freezer recipes; Watertight Steel enabled: {}",
                restored, generatedRecipes, freezerRecipes, canGenerateAlloyBlastRecipe(GTMaterials.WatertightSteel));
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
        material.getProperty(PropertyKey.FLUID).enqueueRegistration(
                FluidStorageKeys.MOLTEN, new FluidBuilder().state(FluidState.LIQUID));
        return true;
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
        return material.getMaterialComponents().stream()
                .allMatch(stack -> stack.material().hasProperty(PropertyKey.DUST) ||
                        stack.material().hasProperty(PropertyKey.FLUID));
    }
}
