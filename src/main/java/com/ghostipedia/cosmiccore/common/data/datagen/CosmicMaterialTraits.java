package com.ghostipedia.cosmiccore.common.data.datagen;

import com.ghostipedia.cosmiccore.common.data.materials.tinkers.TinkersMaterial;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Tiers;

import slimeknights.tconstruct.library.data.material.AbstractMaterialDataProvider;
import slimeknights.tconstruct.library.data.material.AbstractMaterialTraitDataProvider;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.util.LazyModifier;
import slimeknights.tconstruct.tools.stats.StatlessMaterialStats;

import java.util.Map;
import java.util.Set;

public class CosmicMaterialTraits extends AbstractMaterialTraitDataProvider {

    public CosmicMaterialTraits(PackOutput packOutput, AbstractMaterialDataProvider materials) {
        super(packOutput, materials);
    }

    public static final TinkersMaterial TEST_MATERIAL = new TinkersMaterial.Builder("shiboubouya_dn_tube")
            .materialValue(2)
            .headMaterialStats(2200, 10.0f, Tiers.IRON, 5.0f)
            .gripMaterialStats(3000, 10.0f, 20)
            .handleMaterialStats(3000, 10, 4, 10)
            .addStatlessType(StatlessMaterialStats.BINDING)
            .addStatlessType(StatlessMaterialStats.REPAIR_KIT)
            .addStatlessType(StatlessMaterialStats.BOWSTRING)
            //.defaultTrait(CosmicCoreModifiers.wrenchModeSwitch)
            //.trait(CosmicCoreModifiers.wrenchModeSwitch, 3, MaterialRegistry.RANGED)
            .sortOrder(10)
            .craftable(true)
            .build();

    @Override
    protected void addMaterialTraits() {
        for (TinkersMaterial material : TinkersMaterial.MATERIALS) {
            Set<LazyModifier> defaultTraits = material.getDefaultTraits();
            if (!defaultTraits.isEmpty()) {
                addDefaultTraits(material.getMaterialLocation(),
                        material.getDefaultTraits().toArray(defaultTraits.toArray(new LazyModifier[0])));
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
        return "";
    }
}
