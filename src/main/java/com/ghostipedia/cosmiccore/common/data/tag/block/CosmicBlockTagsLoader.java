package com.ghostipedia.cosmiccore.common.data.tag.block;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;

import com.tterrag.registrate.providers.RegistrateTagsProvider;

public class CosmicBlockTagsLoader {

    public static void init(RegistrateTagsProvider<Block> provider) {
        var overworldTag = provider.addTag(CosmicBlockTags.OVERWORLD_ORE_REPLACEABLES)
                .addTag(BlockTags.STONE_ORE_REPLACEABLES)
                .addTag(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

        overworldTag.addOptional(ResourceLocation.fromNamespaceAndPath("create", "scoria"));
        overworldTag.addOptional(ResourceLocation.fromNamespaceAndPath("create", "scorchia"));
        overworldTag.addOptional(ResourceLocation.fromNamespaceAndPath("create", "crimsite"));
        overworldTag.addOptional(ResourceLocation.fromNamespaceAndPath("create", "limestone"));
        overworldTag.addOptional(ResourceLocation.fromNamespaceAndPath("create", "asurine"));
        overworldTag.addOptional(ResourceLocation.fromNamespaceAndPath("create", "ochrum"));
        overworldTag.addOptional(ResourceLocation.fromNamespaceAndPath("create", "veridium"));
    }
}
