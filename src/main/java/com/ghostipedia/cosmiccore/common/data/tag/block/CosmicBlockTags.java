package com.ghostipedia.cosmiccore.common.data.tag.block;

import com.ghostipedia.cosmiccore.common.data.tag.TagUtil;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class CosmicBlockTags {

    public static final TagKey<Block> OVERWORLD_ORE_REPLACEABLES = TagUtil
            .createModBlockTag("overworld_ore_replaceables");

    public static final TagKey<Block> HOLLOW_ORE_REPLACEABLES = TagUtil
            .createModBlockTag("hollow_ore_replaceables");
}
