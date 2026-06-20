package com.ghostipedia.cosmiccore.forge;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.AlloyBlastProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.data.recipe.misc.alloyblast.CustomAlloyBlastRecipeProducer;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ABSModifications {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void addAlloyBlastProperties(PostMaterialEvent event) {
        Material material = CosmicMaterials.ResonantVirtueMeld;
        AlloyBlastProperty property = material.getProperty(PropertyKey.ALLOY_BLAST);
        if (property == null) {
            // TODO(cosmiccore-42.9): GTCEu 8.0 stopped auto-adding an AlloyBlastProperty to alloys with
            // 2+ fluid-only components (here Virtue + Prisma), so add it explicitly to keep this material's
            // ABS recipe. When ABS recipe-gen is re-enabled, confirm a MOLTEN fluid is registered for it too.
            property = new AlloyBlastProperty(material.getBlastTemperature());
            material.setProperty(PropertyKey.ALLOY_BLAST, property);
        }
        property.setRecipeProducer(new CustomAlloyBlastRecipeProducer(-1, -1, 32));
    }
}
