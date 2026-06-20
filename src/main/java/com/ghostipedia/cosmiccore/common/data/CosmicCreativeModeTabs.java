package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.world.item.CreativeModeTab;

import com.tterrag.registrate.util.entry.RegistryEntry;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;

public class CosmicCreativeModeTabs {

    // 1.21 Registrate auto-populates the default tab from the mod's registered entries via its own
    // BuildCreativeModeTabContentsEvent handler, so we no longer attach a GTCEu RegistrateDisplayItemsGenerator
    // here. Doing both added every entry twice (e.g. the CoilBlocks), which is a hard "already exists" crash
    // whenever the tab (re)builds. Registrate's auto-add is now the single source of truth for this tab.
    public static RegistryEntry<CreativeModeTab, CreativeModeTab> COSMIC_CORE = REGISTRATE.defaultCreativeTab(
            CosmicCore.MOD_ID,
            builder -> builder
                    .title(REGISTRATE.addLang("itemGroup", CosmicCore.id("creative_tab"), "Cosmic Core"))
                    .icon(CosmicItems.DONK::asStack)
                    .build())
            .register();

    public static void init() {}
}
