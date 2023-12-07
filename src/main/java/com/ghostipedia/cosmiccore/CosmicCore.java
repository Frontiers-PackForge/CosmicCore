package com.ghostipedia.cosmiccore;

import com.ghostipedia.cosmiccore.api.registries.CosmicRegistries;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.data.CosmicCreativeModeTabs;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("cosmiccore")
public class CosmicCore {
      public static final String
            MOD_ID = "cosmiccore",
            NAME = "CosmicCore";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    public CosmicCore() {
        CosmicCore.init();
    }

    public static void init() {

        // ConfigHolder.init(); // Forcefully init GT config because fabric doesn't allow dependents to load after dependencies
        CosmicCreativeModeTabs.init();
        CosmicBlocks.init();
        CosmicItems.init();
        CosmicRegistries.REGISTRATE.registerRegistrate();
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}