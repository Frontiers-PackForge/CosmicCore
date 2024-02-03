package com.ghostipedia.cosmiccore;

import com.ghostipedia.cosmiccore.api.registries.CosmicRegistries;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.data.CosmicCreativeModeTabs;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.gtbridge.CosmicCoreRecipeTypes;
import com.ghostipedia.cosmiccore.gtbridge.machines.CosmicCoreMachines;
import com.ghostipedia.cosmiccore.gtbridge.machines.MachineCaps;
import com.ghostipedia.cosmiccore.gtbridge.pipenet.blockentities.CosmicBlockEntities;
import com.ghostipedia.cosmiccore.gtbridge.recipe.CosmicRecipeCaps;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.config.ConfigHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




@Mod(CosmicCore.MOD_ID)
public class CosmicCore {
    public static final String
            MOD_ID = "cosmiccore",
            NAME = "CosmicCore";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);
    public static MaterialRegistry MATERIAL_REGISTRY;
    //Init Everything
    public CosmicCore() {
        CosmicCore.init();
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(this);
        bus.addGenericListener(GTRecipeType.class, this::registerRecipeTypes);
        bus.addGenericListener(RecipeCapability.class, this::registerRecipeCaps);
       // bus.addGenericListener(Class.class, this::registerRecipeConditions);
        bus.addGenericListener(MachineDefinition.class, this::registerMachines);
        bus.addListener(this::registerCapabilities);


    }
    public void registerCapabilities(RegisterCapabilitiesEvent event) {MachineCaps.register(event);
    }

    public void registerRecipeCaps(GTCEuAPI.RegisterEvent<String, RecipeCapability<?>> event) {
        CosmicRecipeCaps.init();
    }

    public static void init() {
        ConfigHolder.init();
        CosmicCreativeModeTabs.init();
        CosmicBlocks.init();
        CosmicBlockEntities.init();
        CosmicItems.init();
        CosmicRegistries.COSMIC_REGISTRATE.registerRegistrate();



    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public void registerMaterialRegistry(MaterialRegistryEvent event) {
        MATERIAL_REGISTRY = GTCEuAPI.materialManager.createRegistry(CosmicCore.MOD_ID);
    }


    public void registerRecipeTypes(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {
        CosmicCoreRecipeTypes.init();
    }

    public void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        CosmicCoreMachines.init();
    }
}