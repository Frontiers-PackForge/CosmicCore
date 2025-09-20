package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.item.tcon.CosmicCreativeTinkersTab;

import com.ghostipedia.cosmiccore.common.item.tcon.CosmicCreativeTinkersTab;
import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;

import net.minecraft.world.item.CreativeModeTab;

import com.tterrag.registrate.util.entry.RegistryEntry;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;

public class CosmicCreativeModeTabs {

    public static RegistryEntry<CreativeModeTab> COSMIC_CORE = REGISTRATE.defaultCreativeTab(CosmicCore.MOD_ID,
            builder -> builder
                    .displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator(CosmicCore.MOD_ID, REGISTRATE))
                    .title(REGISTRATE.addLang("itemGroup", CosmicCore.id("creative_tab"), "Cosmic Core"))
                    .icon(CosmicItems.DONK::asStack)
                    .build())
            .register();

    public static RegistryEntry<CreativeModeTab> COSMIC_CORE_TINKERS_TOOLS = REGISTRATE
            .defaultCreativeTab(CosmicCore.MOD_ID,
                    builder -> builder
                            .displayItems(CosmicCreativeTinkersTab::addCreativeTabItems)
                            .title(REGISTRATE.addLang("itemGroup", CosmicCore.id("creative_tab_tinker_tools"),
                                    "Cosmic Core Tinkers Compat"))
                            .icon(CosmicItems.RUNE_CONJUNCTION_VALKRUTH::asStack)
                            .build())
            .register();

    public static void init() {}
}
