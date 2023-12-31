package com.ghostipedia.cosmiccore;

import com.ghostipedia.cosmiccore.api.registries.CosmicRegistries;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.data.CosmicCreativeModeTabs;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.gregtechceu.gtceu.config.ConfigHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




@Mod("cosmiccore")
public class CosmicCore {
    public static final String
            MOD_ID = "cosmiccore",
            NAME = "CosmicCore";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);
    //Init Everything
    public CosmicCore() {

        ConfigHolder.init();

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        CosmicCreativeModeTabs.init();
        CosmicBlocks.init();
        CosmicItems.init();
        CosmicRegistries.REGISTRATE.registerRegistrate();

    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}