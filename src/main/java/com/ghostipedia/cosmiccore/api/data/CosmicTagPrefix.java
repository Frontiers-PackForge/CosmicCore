package com.ghostipedia.cosmiccore.api.data;

import com.ghostipedia.cosmiccore.common.data.tag.TagUtil;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Predicate;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_ROD;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.Conditions.hasIngotProperty;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.Conditions.hasOreProperty;

public class CosmicTagPrefix {

    public static TagPrefix crushedLeached;
    public static TagPrefix prismaFrothed;
    public static TagPrefix ultraDense;
    public static TagPrefix heavyBeam;
    public static TagPrefix modularShelling;
    public static TagPrefix plasmites;
    public static TagPrefix largeWireSpool;
    public static TagPrefix alveFoilInsulator;
    public static TagPrefix shapeMemoryFoil;
    public static final TagKey<Block> STAR_LADDER_BLOCKS = TagUtil.createBlockTag("starladder_blocks");
    public static final TagKey<Item> STAR_LADDER_ITEMS = TagUtil.createItemTag("starladder_items");

    public static final Predicate<Material> hasWireProp = material -> material.hasProperty(PropertyKey.WIRE);
    public static final Predicate<Material> hasPlateProp = material -> material.hasFlag(MaterialFlags.GENERATE_PLATE);
    public static final Predicate<Material> hasRodProp = material -> material.hasFlag(GENERATE_ROD);
    public static final Predicate<Material> hasFrameProp = material -> material.hasFlag(MaterialFlags.GENERATE_FRAME);
    public static final Predicate<Material> hasBoltProp = material -> material
            .hasFlag(MaterialFlags.GENERATE_BOLT_SCREW);
    public static final Predicate<Material> hasFineWireProp = material -> material
            .hasFlag(MaterialFlags.GENERATE_FINE_WIRE);

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
                .maxStackSize(1)
                .generationCondition(hasPlateProp);

        heavyBeam = new TagPrefix("heavyBeam")
                .idPattern("heavy_%s_beam")
                .defaultTagPath("heavy_beams/%s")
                .defaultTagPath("heavy_beams")
                .materialIconType(CosmicCoreMaterialIconType.heavyBeam)
                .unificationEnabled(true)
                .generateItem(true)
                .maxStackSize(16)
                .generationCondition(
                        hasPlateProp
                                .and(hasRodProp));
        modularShelling = new TagPrefix("modular_shelling")
                .idPattern("%s_modular_shelling")
                .defaultTagPath("modular_shellings/%s")
                .defaultTagPath("modular_shellings")
                .materialIconType(CosmicCoreMaterialIconType.modularShelling)
                .unificationEnabled(true)
                .generateItem(true)
                .maxStackSize(16)
                .generationCondition(hasPlateProp.and(hasFrameProp).and(hasBoltProp));
        plasmites = new TagPrefix("plasmites")
                .idPattern("%s_plasmites")
                .defaultTagPath("plasmites/%s")
                .defaultTagPath("plasmites")
                .materialIconType(CosmicCoreMaterialIconType.plasmites)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(hasIngotProperty);

        largeWireSpool = new TagPrefix("large_wire_spool")
                .idPattern("%s_wire_spool")
                .defaultTagPath("wire_spools/%s")
                .defaultTagPath("wire_spools")
                .materialIconType(CosmicCoreMaterialIconType.wireSpool)
                .unificationEnabled(true)
                .generateItem(true)
                .maxStackSize(4)
                .generationCondition(hasWireProp.or(hasFineWireProp));

        alveFoilInsulator = new TagPrefix("alveFoilInsulator")
                .idPattern("%s_alve_foil_insulator")
                .defaultTagPath("alve_foil_insulators/%s")
                .defaultTagPath("alve_foil_insulators")
                .materialIconType(CosmicCoreMaterialIconType.alveFoil)
                .unificationEnabled(true)
                .generateItem(true)
                .maxStackSize(64)
                .generationCondition(hasPlateProp.and(hasFineWireProp));

        shapeMemoryFoil = new TagPrefix("shapeMemoryFoil")
                .idPattern("%s_shape_memory_foil")
                .defaultTagPath("shape_memory_foils/%s")
                .defaultTagPath("shape_memory_foils")
                .materialIconType(CosmicCoreMaterialIconType.memoryFoil)
                .unificationEnabled(true)
                .generateItem(true)
                .maxStackSize(64)
                .generationCondition(hasPlateProp.and(hasWireProp).and(hasBoltProp));
    }
}
