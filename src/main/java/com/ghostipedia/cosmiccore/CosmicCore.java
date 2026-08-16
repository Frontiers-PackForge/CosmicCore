package com.ghostipedia.cosmiccore;

import com.ghostipedia.cosmiccore.api.capability.recipe.CosmicRecipeCapabilities;
import com.ghostipedia.cosmiccore.api.data.CosmicCoreMaterialIconType;
import com.ghostipedia.cosmiccore.api.data.CosmicTagPrefix;
import com.ghostipedia.cosmiccore.api.item.LinkedTerminalBehavior;
import com.ghostipedia.cosmiccore.api.pattern.CosmicPredicates;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulIngredient;
import com.ghostipedia.cosmiccore.api.recipe.lookup.MapEmberIngredient;
import com.ghostipedia.cosmiccore.api.recipe.lookup.MapSoulIngredient;
import com.ghostipedia.cosmiccore.api.registries.CosmicRegistration;
import com.ghostipedia.cosmiccore.client.CosmicCoreClient;
import com.ghostipedia.cosmiccore.common.airControl.OxygenRules;
import com.ghostipedia.cosmiccore.common.block.crop.CosmicCropFeatures;
import com.ghostipedia.cosmiccore.common.block.crop.CosmicCrops;
import com.ghostipedia.cosmiccore.common.commands.argument.SoulTypeArgument;
import com.ghostipedia.cosmiccore.common.compat.ars.ArsSealCompat;
import com.ghostipedia.cosmiccore.common.compat.ftbquests.DeedQuestCompatBridge;
import com.ghostipedia.cosmiccore.common.compat.lso.LsoFoodCompat;
import com.ghostipedia.cosmiccore.common.compat.occultism.CosmicRituals;
import com.ghostipedia.cosmiccore.common.config.CosmicCoreConfig;
import com.ghostipedia.cosmiccore.common.data.*;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicBundleMaterials;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicCrystallizationMaterials;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicElements;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterialSet;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;
import com.ghostipedia.cosmiccore.common.data.recipe.CosmicCoreOreRecipeHandler;
import com.ghostipedia.cosmiccore.common.data.temperature.CosmicTemperatureModifiers;
import com.ghostipedia.cosmiccore.common.data.worldgen.CosmicWorldGenLayers;
import com.ghostipedia.cosmiccore.common.data.worldgen.firmament.CosmicFirmamentFeatures;
import com.ghostipedia.cosmiccore.common.data.worldgen.generator.CosmicChunkGenerators;
import com.ghostipedia.cosmiccore.common.firmament.FirmamentSpaceGravityCompat;
import com.ghostipedia.cosmiccore.common.item.armor.boots.TravelerBootsMigration;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm.BloomwyrmChunkLoading;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.modular.MultiblockInit;
import com.ghostipedia.cosmiccore.common.mob.DimensionMobScaling;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.recipe.condition.CosmicConditions;
import com.ghostipedia.cosmiccore.ember.CosmicEmberCapabilities;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.MapIngredientTypeManager;
import com.gregtechceu.gtceu.common.block.CoilBlock;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import appeng.api.features.GridLinkables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(CosmicCore.MOD_ID)
public class CosmicCore {

    // GTCEu 8.0 registers all content during a single RegisterEvent; guard so it only runs once.
    // Content registration mirrors GTCEu's CommonProxy#onRegister ordering (elements -> materials -> tag prefixes
    // -> recipe caps, conditions, and types -> blocks -> items -> machines -> sounds).
    private static boolean didRunRegistration = false;
    public static final String MOD_ID = "cosmiccore", NAME = "CosmicCore";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public CosmicCore(IEventBus modBus, ModContainer modContainer) {
        modBus.register(this);
        modContainer.registerConfig(ModConfig.Type.CLIENT, CosmicCoreConfig.CLIENT_SPEC);
        CosmicRegistration.REGISTRATE.registerEventListeners(modBus);
        CosmicAttachmentTypes.ATTACHMENT_TYPES.register(modBus);
        CosmicEffects.EFFECTS.register(modBus);
        CosmicParticleTypes.PARTICLE_TYPES.register(modBus);
        CosmicLootModifiers.register(modBus);
        CosmicTemperatureModifiers.register(modBus);
        CosmicChunkGenerators.register(modBus);
        CosmicCropFeatures.register(modBus);
        CosmicFirmamentFeatures.register(modBus);
        CosmicRituals.register(modBus);

        if (FMLEnvironment.dist.isClient()) {
            CosmicCoreClient.init(modBus);
        }
    }

    @SubscribeEvent
    public void registerTicketControllers(RegisterTicketControllersEvent event) {
        BloomwyrmChunkLoading.register(event);
    }

    @SubscribeEvent
    public void onRegister(RegisterEvent event) {
        CosmicRegistryAliases.register(event);
        if (didRunRegistration) return;
        didRunRegistration = true;

        ConfigHolder.init();
        CosmicCreativeModeTabs.init();
        CosmicElements.init();
        CosmicMaterials.register();
        CosmicBundleMaterials.register();
        CosmicCrystallizationMaterials.register();
        CosmicCoreMaterialIconType.init();
        CosmicTagPrefix.initTagPrefixes();
        CosmicMaterialSet.init();
        CosmicRecipeCapabilities.init();
        CosmicConditions.register();
        CosmicRecipeTypes.init();
        CosmicBlocks.init();
        CosmicBlockEntities.init();
        CosmicCovers.init();
        CosmicItems.init();
        CosmicCoverItems.init();
        CosmicBotanyItemRegistration.init();
        CosmicCrops.init();
        CosmicPredicates.init();
        MultiblockInit.init();
        CosmicMachines.init();
        CosmicSounds.init();
        CosmicWorldGenLayers.init();
        CosmicCoreDatagen.init();
    }

    @SubscribeEvent
    public void modifyExistingMaterials(PostMaterialEvent event) {
        CosmicMaterials.modifyMaterials();
    }

    @SubscribeEvent
    public void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CosmicRegistryAliases.validatePetrochemicalAliases();
            MapIngredientTypeManager.registerMapIngredient(SoulIngredient.class, MapSoulIngredient::from);
            TravelerBootsMigration.registerAliases();
            MapIngredientTypeManager.registerMapIngredient(Double.class, MapEmberIngredient::convertToMapIngredient);
            GridLinkables.register(CosmicItems.LINKED_TERMINAL, LinkedTerminalBehavior.handler);
            OxygenRules.registerAirRanges();
            DimensionMobScaling.registerScaling();
            ArgumentTypeInfos.registerByClass(SoulTypeArgument.class,
                    SingletonArgumentInfo.contextFree(SoulTypeArgument::soulType));
            CosmicCoreOreRecipeHandler.disableBundleCauldronWash();
            DeedQuestCompatBridge.register();
            FirmamentSpaceGravityCompat.registerPushSurfaceProvider();
            if (LsoFoodCompat.isLoaded()) {
                LsoFoodCompat.retuneEffects();
            }
            if (ArsSealCompat.isLoaded()) {
                ArsSealCompat.register();
            }
        });
    }

    @SubscribeEvent
    public void onLoadComplete(FMLLoadCompleteEvent event) {
        GTCEuAPI.HEATING_COILS.remove(CoilBlock.CoilType.RTMALLOY);
        GTCEuAPI.HEATING_COILS.remove(CoilBlock.CoilType.HSSG);
        GTCEuAPI.HEATING_COILS.remove(CoilBlock.CoilType.NAQUADAH);
        GTCEuAPI.HEATING_COILS.remove(CoilBlock.CoilType.TRINIUM);
        GTCEuAPI.HEATING_COILS.remove(CoilBlock.CoilType.TRITANIUM);
    }

    @SubscribeEvent
    public void registerPayloads(RegisterPayloadHandlersEvent event) {
        CCoreNetwork.registerPayloads(event);
    }

    @SubscribeEvent
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        CosmicEmberCapabilities.register(event);
    }
}
