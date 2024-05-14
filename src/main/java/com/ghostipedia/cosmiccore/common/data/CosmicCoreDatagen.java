package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.api.registries.CosmicRegistration;
import com.ghostipedia.cosmiccore.common.data.lang.CosmicLangHandler;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.tterrag.registrate.providers.ProviderType;

public class CosmicCoreDatagen {
    public static void init() {
        //TODO: IDK IF I NEED THESE YET, MIGHT BE NEEDED IDK
//        CosmicRegistration.REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, TagsHandler::initItem);
//        CosmicRegistration.REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, TagsHandler::initBlock);
//        CosmicRegistration.REGISTRATE.addDataGenerator(ProviderType.FLUID_TAGS, TagsHandler::initFluid);
//        CosmicRegistration.REGISTRATE.addDataGenerator(ProviderType.ENTITY_TAGS, TagsHandler::initEntity);
        CosmicRegistration.REGISTRATE.addDataGenerator(ProviderType.LANG, CosmicLangHandler::init);
    }
}
