package com.ghostipedia.cosmiccore.common.data.tag;

import com.ghostipedia.cosmiccore.common.data.tag.item.CosmicItemTagsLoader;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class CosmicTagsHandler {

    public static void initItem(RegistrateTagsProvider<Item> provider) {
        CosmicItemTagsLoader.init(provider);
    }

    public static void initBlock(RegistrateTagsProvider<Block> provider) {}

    public static void initFluid(RegistrateTagsProvider<Fluid> provider) {}

    public static void initEntity(RegistrateTagsProvider<EntityType<?>> provider) {}

}
