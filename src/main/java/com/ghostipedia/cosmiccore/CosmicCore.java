package com.ghostipedia.cosmiccore;

import com.ghostipedia.cosmiccore.api.capability.CosmicCapabilities;
import com.ghostipedia.cosmiccore.api.item.LinkedTerminalBehavior;
import com.ghostipedia.cosmiccore.api.pattern.CosmicPredicates;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.TinkerIngredient;
import com.ghostipedia.cosmiccore.api.recipe.lookup.MapSoulIngredient;
import com.ghostipedia.cosmiccore.api.registries.CosmicRegistration;
import com.ghostipedia.cosmiccore.client.CosmicCoreClient;
import com.ghostipedia.cosmiccore.common.data.*;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterialSet;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;
import com.ghostipedia.cosmiccore.common.item.behavior.GravityCoreBehavior;
import com.ghostipedia.cosmiccore.common.item.tcon.CosmicTconBlockTagProvider;
import com.ghostipedia.cosmiccore.common.item.tcon.CosmicTconItemTagProvider;
import com.ghostipedia.cosmiccore.common.item.tcon.CosmicTinkerTools;
import com.ghostipedia.cosmiccore.common.item.tcon.*;
import com.ghostipedia.cosmiccore.common.item.tcon.CosmicToolDefinitionProvider;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.modular.MultiblockInit;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.recipe.condition.CosmicConditions;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.MapIngredientTypeManager;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.common.block.CoilBlock;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import appeng.api.features.GridLinkables;
import earth.terrarium.adastra.api.events.AdAstraEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(CosmicCore.MOD_ID)
public class CosmicCore {

    public static final String MOD_ID = "cosmiccore", NAME = "CosmicCore";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);
    public static MaterialRegistry MATERIAL_REGISTRY;

    // Init Everything
    public CosmicCore(FMLJavaModLoadingContext context) {
        CosmicCore.init();
        var bus = context.getModEventBus();
        bus.register(this);
        bus.addGenericListener(GTRecipeType.class, this::registerRecipeTypes);
        bus.addGenericListener(RecipeConditionType.class, this::registerConditions);
        bus.addGenericListener(MachineDefinition.class, this::registerMachines);
        bus.addGenericListener(SoundEntry.class, this::registerSounds);
        AdAstraEvents.EntityGravityEvent.register(GravityCoreBehavior::clampGravity);

        CosmicLootModifiers.register(bus);

        if (Platform.isClient()) {
            CosmicCoreClient.init(bus);
        }
    }

    public static void init() {
        ConfigHolder.init();
        CosmicCreativeModeTabs.init();
        CosmicBlocks.init();
        CosmicBlockEntities.init();
        CosmicItems.init();
        CosmicTinkerTools.init();
        CosmicTinkerToolPart.init();
        CosmicBotanyItemRegistration.init();
        CosmicRegistration.REGISTRATE.registerRegistrate();
        CosmicCoreDatagen.init();
        CosmicPredicates.init();
        CosmicMaterialSet.init();
        CosmicCreativeModeTabs.init();
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    @SubscribeEvent
    public void gatherDataEvent(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        boolean server = event.includeServer();
        boolean client = event.includeClient();
        CosmicTconBlockTagProvider blockTags = new CosmicTconBlockTagProvider(packOutput, event.getLookupProvider(),
                existingFileHelper);
        generator.addProvider(server, blockTags);
        generator.addProvider(server, new CosmicToolDefinitionProvider(packOutput));
        generator.addProvider(server, new CosmicTconItemTagProvider(packOutput, event.getLookupProvider(),
                blockTags.contentsGetter(), existingFileHelper));
    }

    @SubscribeEvent
    public void registerMaterialRegistry(MaterialRegistryEvent event) {
        MATERIAL_REGISTRY = GTCEuAPI.materialManager.createRegistry(CosmicCore.MOD_ID);
    }

    @SubscribeEvent
    public void registerMaterials(MaterialEvent event) {
        CosmicMaterials.register();
    }

    @SubscribeEvent
    public void modifyExistingMaterials(PostMaterialEvent event) {
        CosmicMaterials.modifyMaterials();
    }

    @SubscribeEvent
    public void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            MapIngredientTypeManager.registerMapIngredient(Integer.class, MapSoulIngredient::convertToMapIngredient);
            GridLinkables.register(CosmicItems.LINKED_TERMINAL, LinkedTerminalBehavior.handler);
            CraftingHelper.register(TinkerIngredient.TYPE, TinkerIngredient.SERIALIZER);
            CCoreNetwork.init();
        });
    }

    @SubscribeEvent
    public void onLoadComplete(FMLLoadCompleteEvent event) {
        GTCEuAPI.HEATING_COILS.remove(CoilBlock.CoilType.RTMALLOY);
        GTCEuAPI.HEATING_COILS.remove(CoilBlock.CoilType.HSSG);
        GTCEuAPI.HEATING_COILS.remove(CoilBlock.CoilType.NAQUADAH);
        GTCEuAPI.HEATING_COILS.remove(CoilBlock.CoilType.TRINIUM);
        GTCEuAPI.HEATING_COILS.remove(CoilBlock.CoilType.TRITANIUM);
        // GCyMMachines.PARALLEL_HATCH = (MachineDefinition[]) Arrays.stream(GCyMMachines.PARALLEL_HATCH).filter(p ->
        // p.getTier() < GTValues.ZPM).toArray();
    }

    public void registerConditions(GTCEuAPI.RegisterEvent<String, RecipeConditionType<?>> event) {
        CosmicConditions.register();
    }

    public void registerRecipeTypes(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {
        CosmicRecipeTypes.init();
    }

    public void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        MultiblockInit.init();
        CosmicMachines.init();
    }

    public void registerSounds(GTCEuAPI.RegisterEvent<ResourceLocation, SoundEntry> event) {
        CosmicSounds.init();
    }

    @SubscribeEvent
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        CosmicCapabilities.register(event);
    }
}
