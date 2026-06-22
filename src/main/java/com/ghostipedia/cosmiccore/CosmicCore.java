package com.ghostipedia.cosmiccore;

import com.ghostipedia.cosmiccore.api.capability.recipe.CosmicRecipeCapabilities;
import com.ghostipedia.cosmiccore.api.data.CosmicCoreMaterialIconType;
import com.ghostipedia.cosmiccore.api.data.CosmicTagPrefix;
import com.ghostipedia.cosmiccore.api.item.LinkedTerminalBehavior;
import com.ghostipedia.cosmiccore.api.pattern.CosmicPredicates;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulIngredient;
import com.ghostipedia.cosmiccore.api.recipe.lookup.MapSoulIngredient;
import com.ghostipedia.cosmiccore.api.registries.CosmicRegistration;
import com.ghostipedia.cosmiccore.client.CosmicCoreClient;
import com.ghostipedia.cosmiccore.common.airControl.OxygenRules;
import com.ghostipedia.cosmiccore.common.commands.argument.SoulTypeArgument;
import com.ghostipedia.cosmiccore.common.data.*;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicBundleMaterials;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicElements;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterialSet;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.modular.MultiblockInit;
import com.ghostipedia.cosmiccore.common.mob.DimensionMobScaling;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.recipe.condition.CosmicConditions;
import com.ghostipedia.cosmiccore.common.reflection.bargain.CosmicBargains;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.MapIngredientTypeManager;
import com.gregtechceu.gtceu.common.block.CoilBlock;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
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

    public CosmicCore(IEventBus modBus) {
        modBus.register(this);
        CosmicRegistration.REGISTRATE.registerEventListeners(modBus);
        CosmicAttachmentTypes.ATTACHMENT_TYPES.register(modBus);
        CosmicLootModifiers.register(modBus);
        CosmicBargains.init();

        if (Platform.isClient()) {
            CosmicCoreClient.init(modBus);
        }
    }

    @SubscribeEvent
    public void onRegister(RegisterEvent event) {
        if (didRunRegistration) return;
        didRunRegistration = true;

        ConfigHolder.init();
        CosmicCreativeModeTabs.init();
        CosmicElements.init();
        CosmicMaterials.register();
        CosmicBundleMaterials.register();
        CosmicCoreMaterialIconType.init();
        CosmicTagPrefix.initTagPrefixes();
        CosmicMaterialSet.init();
        CosmicRecipeCapabilities.init();
        CosmicConditions.register();
        CosmicRecipeTypes.init();
        CosmicBlocks.init();
        CosmicBlockEntities.init();
        CosmicItems.init();
        CosmicBotanyItemRegistration.init();
        CosmicPredicates.init();
        MultiblockInit.init();
        CosmicMachines.init();
        CosmicSounds.init();
        CosmicCoreDatagen.init();
    }

    @SubscribeEvent
    public void modifyExistingMaterials(PostMaterialEvent event) {
        CosmicMaterials.modifyMaterials();
    }

    @SubscribeEvent
    public void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            MapIngredientTypeManager.registerMapIngredient(SoulIngredient.class, MapSoulIngredient::from);
            GridLinkables.register(CosmicItems.LINKED_TERMINAL, LinkedTerminalBehavior.handler);
            OxygenRules.registerAirRanges();
            DimensionMobScaling.registerScaling();
            ArgumentTypeInfos.registerByClass(SoulTypeArgument.class,
                    SingletonArgumentInfo.contextFree(SoulTypeArgument::soulType));
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
}
