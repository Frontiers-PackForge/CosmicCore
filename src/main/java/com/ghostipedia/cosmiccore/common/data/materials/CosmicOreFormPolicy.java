package com.ghostipedia.cosmiccore.common.data.materials;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Set;

public final class CosmicOreFormPolicy {

    private static final Set<ResourceLocation> EXPOSED_RAW_ORE_MATERIALS = Set.of(
            GTCEu.id("alunite"),
            CosmicCore.id("moondrop"),
            CosmicCore.id("reclaimed_pale_ore"));
    private static final Set<TagPrefix> STANDARD_ORE_SUBSTEP_PREFIXES = Set.of(
            TagPrefix.rawOre,
            TagPrefix.rawOreBlock,
            TagPrefix.crushed,
            TagPrefix.crushedPurified,
            TagPrefix.crushedRefined,
            TagPrefix.dustImpure,
            TagPrefix.dustPure);

    private CosmicOreFormPolicy() {}

    public static boolean isUnusedGeneratedOreForm(Item item) {
        MaterialEntry entry = ChemicalHelper.getMaterialEntry(item);
        if (entry.isEmpty()) return false;

        TagPrefix prefix = entry.tagPrefix();
        Material material = entry.material();
        if (prefix.isIgnored(material)) return false;
        if (CosmicBundleMaterials.isBundleOre(material)) return false;

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        if (!itemId.getNamespace().equals(material.getModid())) return false;

        if (TagPrefix.ORES.containsKey(prefix) || TagPrefix.surfaceRock.equals(prefix)) return true;
        if (!STANDARD_ORE_SUBSTEP_PREFIXES.contains(prefix)) return false;

        return !TagPrefix.rawOre.equals(prefix) ||
                !EXPOSED_RAW_ORE_MATERIALS.contains(material.getResourceLocation());
    }
}
