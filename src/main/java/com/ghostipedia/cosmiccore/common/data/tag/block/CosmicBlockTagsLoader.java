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

        overworldTag.addOptional(new ResourceLocation("create", "scoria"));
        overworldTag.addOptional(new ResourceLocation("create", "scorchia"));
        overworldTag.addOptional(new ResourceLocation("create", "crimsite"));
        overworldTag.addOptional(new ResourceLocation("create", "limestone"));
        overworldTag.addOptional(new ResourceLocation("create", "asurine"));
        overworldTag.addOptional(new ResourceLocation("create", "ochrum"));
        overworldTag.addOptional(new ResourceLocation("create", "veridium"));
    }
}
