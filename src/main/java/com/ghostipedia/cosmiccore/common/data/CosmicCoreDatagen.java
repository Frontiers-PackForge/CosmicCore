package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.api.registries.CosmicRegistration;
import com.ghostipedia.cosmiccore.common.data.lang.CosmicLangHandler;
import com.ghostipedia.cosmiccore.common.data.tag.CosmicTagsHandler;

import com.tterrag.registrate.providers.ProviderType;

public class CosmicCoreDatagen {

    public static void init() {
        CosmicRegistration.REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, CosmicTagsHandler::initItem);
        CosmicRegistration.REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, CosmicTagsHandler::initBlock);
        CosmicRegistration.REGISTRATE.addDataGenerator(ProviderType.FLUID_TAGS, CosmicTagsHandler::initFluid);
        CosmicRegistration.REGISTRATE.addDataGenerator(ProviderType.ENTITY_TAGS, CosmicTagsHandler::initEntity);
        CosmicRegistration.REGISTRATE.addDataGenerator(ProviderType.LANG, CosmicLangHandler::init);
    }
}
