package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.item.tcon.CosmicCreativeTinkersTab;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;

import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import net.minecraft.world.item.CreativeModeTab;

import com.tterrag.registrate.util.entry.RegistryEntry;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;

public class CosmicCreativeModeTabs {

    public static final RegistryEntry<CreativeModeTab> COSMIC_CORE =
            REGISTRATE.defaultCreativeTab("main",
                            b -> b
                                    .displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator("main", REGISTRATE))
                                    .title(REGISTRATE.addLang("itemGroup", CosmicCore.id("creative_tab"), "Cosmic Core Main Items"))
                                    .icon(CosmicItems.DONK::asStack)
                                    .build())
                    .register();

    // Tab id: cosmiccore:tinkers_tools
    public static final RegistryEntry<CreativeModeTab> COSMIC_CORE_TINKERS_TOOLS =
            REGISTRATE.defaultCreativeTab("tinkers_tools",
                            b -> b
                                    .displayItems(CosmicCreativeTinkersTab::addCreativeTabItems)
                                    .title(REGISTRATE.addLang("itemGroup.tinkers.parts",
                                            CosmicCore.id("creative_tab_tinker_tools"), "Cosmic Core Tinkers Compat"))
                                    .icon(CosmicItems.RUNE_CONJUNCTION_VALKRUTH::asStack)
                                    .build())
                    .register();



    public static void init() {}
}
