package com.ghostipedia.cosmiccore.forge;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;

import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.data.recipe.misc.alloyblast.CustomAlloyBlastRecipeProducer;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ABSModifications {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void addAlloyBlastProperties(PostMaterialEvent event) {
        CosmicMaterials.ResonantVirtueMeld.getProperty(PropertyKey.ALLOY_BLAST)
                .setRecipeProducer(new CustomAlloyBlastRecipeProducer(-1, -1, 32));
    }
}
