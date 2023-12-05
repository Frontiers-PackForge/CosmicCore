package com.ghostipedia.CosmicCore.common.data;
import com.ghostipedia.CosmicCore.CosmicCore;
import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.world.item.*;

import static com.ghostipedia.CosmicCore.api.registries.CosmicRegistries.REGISTRATE;


public class CosmicCreativeModeTabs {
    public static RegistryEntry<CreativeModeTab> COSMIC_CORE = REGISTRATE.defaultCreativeTab(CosmicCore.MOD_ID,
                    builder -> builder.displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator(CosmicCore.MOD_ID, REGISTRATE))
                            .icon(CosmicItems.DONK::asStack)
                            .build())
            .register();

    public static void init() {

    }
}
