package com.ghostipedia.cosmiccore.integration.kjs;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.capability.souls.SoulType;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.data.CosmicMachines;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicElements;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;
import com.ghostipedia.cosmiccore.common.data.worldgen.CosmicOreVeins;
import com.ghostipedia.cosmiccore.common.data.worldgen.CosmicWorldGenLayers;
import com.ghostipedia.cosmiccore.common.data.worldgen.generator.veins.BranchingVeinGenerator;
import com.ghostipedia.cosmiccore.common.data.worldgen.generator.veins.ClusterVeinGenerator;
import com.ghostipedia.cosmiccore.common.data.worldgen.generator.veins.FractureVeinGenerator;
import com.ghostipedia.cosmiccore.common.data.worldgen.generator.veins.LensVeinGenerator;
import com.ghostipedia.cosmiccore.common.data.worldgen.generator.veins.ShellVeinGenerator;
import com.ghostipedia.cosmiccore.common.data.worldgen.generator.veins.StringerVeinGenerator;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedRegistry;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import dev.latvian.mods.kubejs.client.LangKubeEvent;
import dev.latvian.mods.kubejs.plugin.ClassFilter;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;

/**
 * KubeJS integration for CosmicCore. Exposes the addon's data holders and vein generators as script bindings and
 * lets scripts import the {@code com.ghostipedia.cosmiccore} package. The soul/ember recipe schema + components from
 * the 1.20.1 build are not ported yet (those subsystems are shelved), so registerRecipeSchemas/Components are left
 * to their defaults; CosmicCore recipe types still get GTCEu's default schema via GTCEu's own KubeJS plugin.
 */
public class CosmicCoreKubeJSPlugin implements KubeJSPlugin {

    @Override
    public void registerClasses(ClassFilter filter) {
        filter.allow("com.ghostipedia.cosmiccore");
    }

    @Override
    public void registerBindings(BindingRegistry bindings) {
        bindings.add("CosmicMaterials", CosmicMaterials.class);
        bindings.add("CosmicElements", CosmicElements.class);
        bindings.add("CosmicBlocks", CosmicBlocks.class);
        bindings.add("CosmicMachines", CosmicMachines.class);
        bindings.add("CosmicItems", CosmicItems.class);
        bindings.add("CosmicRecipeTypes", CosmicRecipeTypes.class);
        bindings.add("CosmicSoulTypes", SoulType.class);

        bindings.add("FractureVeinGenerator", FractureVeinGenerator.class);
        bindings.add("BranchingVeinGenerator", BranchingVeinGenerator.class);
        bindings.add("LensVeinGenerator", LensVeinGenerator.class);
        bindings.add("ClusterVeinGenerator", ClusterVeinGenerator.class);
        bindings.add("StringerVeinGenerator", StringerVeinGenerator.class);
        bindings.add("ShellVeinGenerator", ShellVeinGenerator.class);

        bindings.add("CosmicWorldGenLayers", CosmicWorldGenLayers.class);
        bindings.add("CosmicOreVeins", CosmicOreVeins.class);
        bindings.add("CosmicCore", CosmicCore.class);
        bindings.add("CosmicFood", CosmicFoodBinding.class);
        bindings.add("Deeds", DeedsKubeBinding.class);
    }

    @Override
    public void generateLang(LangKubeEvent event) {
        if (!event.lang().equals("en_us")) return;
        for (var deed : DeedRegistry.all()) {
            for (var entry : deed.enUs().entrySet()) {
                event.add(deed.id().getNamespace(), entry.getKey(), entry.getValue());
            }
        }
    }
}
