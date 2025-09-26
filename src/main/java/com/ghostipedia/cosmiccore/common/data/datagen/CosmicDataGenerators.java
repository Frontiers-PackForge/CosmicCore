package com.ghostipedia.cosmiccore.common.data.datagen;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;
import com.ghostipedia.cosmiccore.common.item.tcon.TinkersMaterials;
import com.ghostipedia.cosmiccore.common.item.tcon.modifiers.CosmicModifierProvider;

import com.gregtechceu.gtceu.api.registry.registrate.SoundEntryBuilder;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import slimeknights.tconstruct.tools.data.material.MaterialDataProvider;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CosmicDataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        var registries = event.getLookupProvider();


        boolean server = event.includeServer();
        generator.addProvider(server, new CosmicModifierProvider(packOutput));
        TinkersMaterials.init();
        var  materials = new CosmicTinkersMaterials(packOutput);
        var traits =  new CosmicMaterialTraits(packOutput, materials);
        var stats = new CosmicMaterialStats(packOutput, materials);


        // TODO DATAGEN FOR Materials + stats + traits (server)

        generator.addProvider(server,materials );
        generator.addProvider(server, traits );
        generator.addProvider(server, stats );

        if (event.includeClient()) {
            generator.addProvider(true, new SoundEntryBuilder.SoundEntryProvider(packOutput, CosmicCore.MOD_ID));
        }
    }
}
