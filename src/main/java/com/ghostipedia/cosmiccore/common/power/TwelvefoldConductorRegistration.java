package com.ghostipedia.cosmiccore.common.power;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.registry.registrate.entry.TagPrefixEntry;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.pipelike.cable.Insulation;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public final class TwelvefoldConductorRegistration {

    private static TagPrefixEntry wirePrefix;
    private static TagPrefixEntry cablePrefix;
    private static Insulation wireInsulation;
    private static Insulation cableInsulation;

    private TwelvefoldConductorRegistration() {}

    public static synchronized void registerPrefixesAndInsulations() {
        if (wirePrefix != null) {
            return;
        }
        wirePrefix = REGISTRATE.tagPrefix("wire_gt_twelve")
                .customLookupTable(() -> GTMaterialBlocks.CABLE_BLOCKS)
                .lang("12x %s Wire")
                .miningToolTag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WIRE_CUTTER)
                .materialAmount(GTValues.M * 6)
                .materialIconType(MaterialIconType.wire)
                .unificationEnabled(true)
                .enableRecycling().register();
        cablePrefix = REGISTRATE.tagPrefix("cable_gt_twelve")
                .customLookupTable(() -> GTMaterialBlocks.CABLE_BLOCKS)
                .lang("12x %s Cable")
                .miningToolTag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WIRE_CUTTER)
                .materialAmount(GTValues.M * 6)
                .unificationEnabled(true)
                .enableRecycling().register();
        Insulation.values();
    }

    public static void attachRubberMaterial() {
        if (cablePrefix.value().secondaryMaterials().isEmpty()) {
            cablePrefix
                    .addSecondaryMaterial(new MaterialStack(GTMaterials.Rubber, TagPrefix.plate.materialAmount() * 4));
        }
    }

    public static TagPrefixEntry wirePrefix() {
        return wirePrefix;
    }

    public static TagPrefixEntry cablePrefix() {
        return cablePrefix;
    }

    public static Insulation wireInsulation() {
        return wireInsulation;
    }

    public static Insulation cableInsulation() {
        return cableInsulation;
    }

    public static void bindInsulations(Insulation wire, Insulation cable) {
        wireInsulation = wire;
        cableInsulation = cable;
    }
}
