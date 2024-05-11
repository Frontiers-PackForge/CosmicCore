package com.ghostipedia.cosmiccore.common.data;
import com.ghostipedia.cosmiccore.CosmicCore;
import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistries.REGISTRATE;


public class CosmicCreativeModeTabs {
    public static RegistryEntry<CreativeModeTab> COSMIC_CORE = REGISTRATE.defaultCreativeTab(CosmicCore.MOD_ID,
                    builder -> builder.displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator(CosmicCore.MOD_ID, REGISTRATE))
                            .title(REGISTRATE.addLang("itemGroup", CosmicCore.id("creative_tab"), "Cosmic Core"))
                            .icon(CosmicItems.DONK::asStack)
                            .build())
            .register();

    public static void init() {

    }
}
