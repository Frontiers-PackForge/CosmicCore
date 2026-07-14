package com.ghostipedia.cosmiccore.common.block.crop;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

final class CropBlockTags {

    static final TagKey<Block> RAINBOW_CANE_PLANTABLE_ON = tag("rainbow_cane_plantable_on");
    static final TagKey<Block> SOUL_GOURD_PLANTABLE_ON = tag("soul_gourd_plantable_on");
    static final TagKey<Block> ROCKVINE_SUPPORTS = tag("rockvine_supports");
    static final TagKey<Block> SPOREBEAN_SUPPORTS = tag("sporebeans_supports");
    static final TagKey<Block> DRIFTWEED_PLANTABLE_ON = tag("driftweed_plantable_on");

    private CropBlockTags() {}

    private static TagKey<Block> tag(String path) {
        return TagKey.create(Registries.BLOCK, CosmicCore.id("crops/" + path));
    }
}
