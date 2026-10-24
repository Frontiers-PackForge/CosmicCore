package com.ghostipedia.cosmiccore.api.data;

import com.ghostipedia.cosmiccore.common.data.materials.CosmicBundleMaterials;
import com.ghostipedia.cosmiccore.common.data.tag.TagUtil;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.registry.registrate.entry.TagPrefixEntry;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Predicate;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_ROD;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.Conditions.hasIngotProperty;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class CosmicTagPrefix {

    // public static TagPrefix crushedLeached;
    // public static TagPrefix prismaFrothed;
    public static TagPrefixEntry ultraDense;
    public static TagPrefixEntry heavyBeam;
    public static TagPrefixEntry modularShelling;
    public static TagPrefixEntry plasmites;
    public static TagPrefixEntry largeWireSpool;
    public static TagPrefixEntry alveFoilInsulator;
    public static TagPrefixEntry shapeMemoryFoil;
    public static TagPrefixEntry rawOreCubic;
    public static TagPrefixEntry oreChunk;
    public static TagPrefixEntry powderizedOre;
    public static TagPrefixEntry crystallizedOreChunk;
    public static TagPrefixEntry atomicallyPurifiedOreChunk;
    public static TagPrefixEntry flocculatedOre;
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
        rawOreCubic = REGISTRATE.tagPrefix("raw_ore_cubic")
                .idPattern("raw_%s_cubic_ore")
                .defaultTagPath("cubic_ores/%s")
                .defaultTagPath("cubic_ores")
                .materialIconType(CosmicCoreMaterialIconType.rawOreCubic)
                .unificationEnabled(true)
                .generateItem(false).register();

        oreChunk = REGISTRATE.tagPrefix("ore_chunk")
                .idPattern("%s_ore_chunk")
                .defaultTagPath("ore_chunks/%s")
                .defaultTagPath("ore_chunks")
                .materialIconType(CosmicCoreMaterialIconType.oreChunk)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(CosmicBundleMaterials::isBundleOutput).register();

        powderizedOre = REGISTRATE.tagPrefix("powderized_ore")
                .idPattern("powderized_%s_ore")
                .defaultTagPath("powderized_ores/%s")
                .defaultTagPath("powderized_ores")
                .materialIconType(CosmicCoreMaterialIconType.powderizedOre)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(CosmicBundleMaterials::isBundleOre).register();

        crystallizedOreChunk = REGISTRATE.tagPrefix("crystallized_ore_chunk")
                .idPattern("crystallized_%s_ore_chunk")
                .defaultTagPath("crystallized_ore_chunks/%s")
                .defaultTagPath("crystallized_ore_chunks")
                .materialIconType(CosmicCoreMaterialIconType.crystallizedOreChunk)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(CosmicBundleMaterials::isBundleOre).register();

        atomicallyPurifiedOreChunk = REGISTRATE.tagPrefix("atomically_purified_ore_chunk")
                .idPattern("atomically_purified_%s_ore_chunk")
                .defaultTagPath("atomically_purified_ore_chunks/%s")
                .defaultTagPath("atomically_purified_ore_chunks")
                .materialIconType(CosmicCoreMaterialIconType.atomicallyPurifiedOreChunk)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(CosmicBundleMaterials::isBundleOre).register();

        flocculatedOre = REGISTRATE.tagPrefix("flocculated_ore")
                .idPattern("flocculated_%s_ore")
                .defaultTagPath("flocculated_ores/%s")
                .defaultTagPath("flocculated_ores")
                .materialIconType(CosmicCoreMaterialIconType.flocculatedOre)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(CosmicBundleMaterials::isBundleOre).register();

        ultraDense = REGISTRATE.tagPrefix("ultradense_plate")
                .idPattern("ultradense_%s_plate")
                .defaultTagPath("ultra_dense_plates/%s")
                .defaultTagPath("ultra_dense_plates")
                .materialIconType(CosmicCoreMaterialIconType.ultraDense)
                .unificationEnabled(true)
                .generateItem(true)
                .maxStackSize(1)
                .generationCondition(hasPlateProp).register();

        heavyBeam = REGISTRATE.tagPrefix("heavy_beam")
                .idPattern("heavy_%s_beam")
                .defaultTagPath("heavy_beams/%s")
                .defaultTagPath("heavy_beams")
                .materialIconType(CosmicCoreMaterialIconType.heavyBeam)
                .unificationEnabled(true)
                .generateItem(true)
                .maxStackSize(16)
                .generationCondition(
                        hasPlateProp
                                .and(hasRodProp))
                .register();
        modularShelling = REGISTRATE.tagPrefix("modular_shelling")
                .idPattern("%s_modular_shelling")
                .defaultTagPath("modular_shellings/%s")
                .defaultTagPath("modular_shellings")
                .materialIconType(CosmicCoreMaterialIconType.modularShelling)
                .unificationEnabled(true)
                .generateItem(true)
                .maxStackSize(16)
                .generationCondition(hasPlateProp.and(hasFrameProp).and(hasBoltProp)).register();
        plasmites = REGISTRATE.tagPrefix("plasmites")
                .idPattern("%s_plasmites")
                .defaultTagPath("plasmites/%s")
                .defaultTagPath("plasmites")
                .materialIconType(CosmicCoreMaterialIconType.plasmites)
                .unificationEnabled(true)
                .generateItem(true)
                .generationCondition(hasIngotProperty).register();

        largeWireSpool = REGISTRATE.tagPrefix("large_wire_spool")
                .idPattern("%s_wire_spool")
                .defaultTagPath("wire_spools/%s")
                .defaultTagPath("wire_spools")
                .materialIconType(CosmicCoreMaterialIconType.wireSpool)
                .unificationEnabled(true)
                .generateItem(true)
                .maxStackSize(4)
                .generationCondition(hasWireProp.or(hasFineWireProp)).register();

        alveFoilInsulator = REGISTRATE.tagPrefix("alve_foil_insulator")
                .idPattern("%s_alve_foil_insulator")
                .defaultTagPath("alve_foil_insulators/%s")
                .defaultTagPath("alve_foil_insulators")
                .materialIconType(CosmicCoreMaterialIconType.alveFoil)
                .unificationEnabled(true)
                .generateItem(true)
                .maxStackSize(64)
                .generationCondition(hasPlateProp.and(hasFineWireProp)).register();

        shapeMemoryFoil = REGISTRATE.tagPrefix("shape_memory_foil")
                .idPattern("%s_shape_memory_foil")
                .defaultTagPath("shape_memory_foils/%s")
                .defaultTagPath("shape_memory_foils")
                .materialIconType(CosmicCoreMaterialIconType.memoryFoil)
                .unificationEnabled(true)
                .generateItem(true)
                .maxStackSize(64)
                .generationCondition(hasPlateProp.and(hasBoltProp)).register();
    }
}
