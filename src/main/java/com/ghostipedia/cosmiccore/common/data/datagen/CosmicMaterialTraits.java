package com.ghostipedia.cosmiccore.common.data.datagen;

import com.ghostipedia.cosmiccore.common.data.materials.tinkers.TinkersMaterial;
import com.ghostipedia.cosmiccore.common.item.tcon.modifiers.CosmicCoreModifiers;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Tiers;

import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.client.data.spritetransformer.GreyToColorMapping;
import slimeknights.tconstruct.library.data.material.AbstractMaterialDataProvider;
import slimeknights.tconstruct.library.data.material.AbstractMaterialTraitDataProvider;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.util.LazyModifier;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.stats.StatlessMaterialStats;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

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
            .defaultTrait(CosmicCoreModifiers.wrenchModeSwitch.getId())
            .trait(()->new ModifierEntry(CosmicCoreModifiers.wrenchModeSwitch.get(), 3), MaterialRegistry.RANGED)
            .trait(()->new ModifierEntry(TinkerModifiers.decay.get(), 1), MaterialRegistry.MELEE_HARVEST)
            .sortOrder(10)
            .craftable(true)
            .colorMapping(
                    GreyToColorMapping.builder()
                            .addARGB(0,   0xFFFF66CC)
                            .addARGB(85,  0xFFFF99DD)
                            .addARGB(170, 0xFF99CCFF)
                            .addARGB(255, 0xFF66CCFF)
                            .build()
            )
            .fallback("silver")
            .fallback("iron")
            .color(0xFFADD8E6)
            .Ingredient(Ingredient.of(TagPrefix.ingot.getItemTags(GTMaterials.Neutronium)[0]), 1, 2)
            .build();

    @Override
    protected void addMaterialTraits() {
        for (TinkersMaterial material : TinkersMaterial.MATERIALS) {
            noTraits(material.getMaterialLocation());
            Set<LazyModifier> defaultTraits = material.getDefaultTraits();
            if (!defaultTraits.isEmpty()) {
                for(var defaultTrait : defaultTraits){
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
