package com.ghostipedia.cosmiccore.common.item.tcon;

import com.ghostipedia.cosmiccore.common.data.materials.tinkers.TinkersMaterial;
import com.ghostipedia.cosmiccore.common.item.tcon.modifiers.CosmicCoreModifiers;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.tconstruct.library.client.data.spritetransformer.GreyToColorMapping;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.stats.StatlessMaterialStats;

public class TinkersMaterials {

    public static void init() {

        for(Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
            if (!material.hasProperty(PropertyKey.INGOT)){
                continue;
            }

            if(material.hasProperty(PropertyKey.TOOL)){

                 new TinkersMaterial.Builder(material.getName())
                        .materialValue(2)
                        .headMaterialStats(2200, 10.0f, Tiers.IRON, 5.0f)
                        .gripMaterialStats(3000, 10.0f, 20)
                        .handleMaterialStats(3000, 10, 4, 10)
                        .addStatlessType(StatlessMaterialStats.BINDING)
                        .addStatlessType(StatlessMaterialStats.REPAIR_KIT)
                        .addStatlessType(StatlessMaterialStats.BOWSTRING)
                        .defaultTrait(CosmicCoreModifiers.wrenchModeSwitch.getId())
                        .trait(() -> new ModifierEntry(CosmicCoreModifiers.wrenchModeSwitch.get(), 3), MaterialRegistry.RANGED)
                        .trait(() -> new ModifierEntry(TinkerModifiers.decay.get(), 1), MaterialRegistry.MELEE_HARVEST)
                        .sortOrder(10)
                        .craftable(true)
                        .colorMapping(
                                GreyToColorMapping.builder()
                                        .addARGB(0, 0xFFFF66CC)
                                        .addARGB(85, 0xFFFF99DD)
                                        .addARGB(170, 0xFF99CCFF)
                                        .addARGB(255, 0xFF66CCFF)
                                        .build())
                        .fallback("silver")
                        .fallback("iron")
                        .color(material.getMaterialARGB())
                        .Ingredient(Ingredient.of(ChemicalHelper.getIngot(material, GTValues.M)), 1, 2)
                        .build();



            }

        }
    }
}
