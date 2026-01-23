package com.ghostipedia.cosmiccore.common.data.datagen;

import com.ghostipedia.cosmiccore.common.data.materials.tinkers.TinkersMaterial;
import com.ghostipedia.cosmiccore.common.item.tcon.modifiers.CosmicCoreModifiers;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;

import slimeknights.tconstruct.library.client.data.spritetransformer.GreyToColorMapping;
import slimeknights.tconstruct.library.data.material.AbstractMaterialDataProvider;
import slimeknights.tconstruct.library.data.material.AbstractMaterialTraitDataProvider;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.util.LazyModifier;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.stats.StatlessMaterialStats;

import java.util.Map;
import java.util.Set;

public class CosmicMaterialTraits extends AbstractMaterialTraitDataProvider {

    public CosmicMaterialTraits(PackOutput packOutput, AbstractMaterialDataProvider materials) {
        super(packOutput, materials);
    }

    @Override
    protected void addMaterialTraits() {
        for (TinkersMaterial material : TinkersMaterial.MATERIALS) {
            noTraits(material.getMaterialLocation());
            Set<LazyModifier> defaultTraits = material.getDefaultTraits();
            if (!defaultTraits.isEmpty()) {
                for (var defaultTrait : defaultTraits) {
                    addDefaultTraits(material.getMaterialLocation(), defaultTrait);
                }
            }
            Map<MaterialStatsId, Set<ModifierEntry>> statTraits = material.getTraits();
            if (!statTraits.isEmpty()) {
                for (Map.Entry<MaterialStatsId, Set<ModifierEntry>> entry : statTraits.entrySet()) {
                    addTraits(
                            material.getMaterialLocation(),
                            entry.getKey(),
                            entry.getValue().toArray(new ModifierEntry[0]));
                }
            }

        }
    }

    @Override
    public String getName() {
        return "Cosmic Material Traits ";
    }
}
