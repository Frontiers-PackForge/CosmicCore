package com.ghostipedia.cosmiccore.api.data;

import com.ghostipedia.cosmiccore.common.data.tag.TagUtil;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.Conditions.hasIngotProperty;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.Conditions.hasOreProperty;

public class CosmicCustomTags {

    public static TagPrefix crushedLeached;
    public static TagPrefix prismaFrothed;
    public static TagPrefix ultraDense;
    public static final TagKey<Block> STAR_LADDER_BLOCKS = TagUtil.createBlockTag("starladder_blocks");
    public static final TagKey<Item> STAR_LADDER_ITEMS = TagUtil.createItemTag("starladder_items");

    public static void initTagPrefixes() {
        crushedLeached = new TagPrefix("leachedOre")
                .idPattern("leached_%s_ore")
                .defaultTagPath("leached_ores/%s")
                .defaultTagPath("leached_ores")
                .materialIconType(CosmicCoreMaterialIconType.crushedLeached)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(hasOreProperty);
        prismaFrothed = new TagPrefix("prismaFrothedOre")
                .idPattern("prisma_frothed_%s_ore")
                .defaultTagPath("prisma_frothed_ores/%s")
                .defaultTagPath("prisma_frothed_ores")
                .materialIconType(CosmicCoreMaterialIconType.prismaFrothed)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(hasOreProperty);

        ultraDense = new TagPrefix("ultradensePlate")
                .idPattern("ultradense_%s_plate")
                .defaultTagPath("ultra_dense_plates/%s")
                .defaultTagPath("ultra_dense_plates")
                .materialIconType(CosmicCoreMaterialIconType.ultraDense)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(hasIngotProperty);
    }
}
