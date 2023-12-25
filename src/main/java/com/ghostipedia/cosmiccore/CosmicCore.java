package com.ghostipedia.cosmiccore;

import com.ghostipedia.cosmiccore.api.registries.CosmicRegistries;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.data.CosmicCreativeModeTabs;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.gtbridge.CosmicCoreMachines;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;




@Mod("cosmiccore")
public class CosmicCore {
    public static final String
            MOD_ID = "cosmiccore",
            NAME = "CosmicCore";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    public CosmicCore() {
    IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static void init() {

        // ConfigHolder.init(); // Forcefully init GT config because fabric doesn't allow dependents to load after dependencies
        CosmicCreativeModeTabs.init();
        CosmicCoreMachines.init();
        CosmicBlocks.init();
        CosmicItems.init();
        CosmicRegistries.REGISTRATE.registerRegistrate();
    }
    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("lol");
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}