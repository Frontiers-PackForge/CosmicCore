package com.ghostipedia.cosmiccore.common.data.datagen;

import com.ghostipedia.cosmiccore.common.data.materials.tinkers.TinkersMaterial;
import slimeknights.tconstruct.library.client.data.material.AbstractMaterialSpriteProvider;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.tools.stats.StatlessMaterialStats;

public class CosmicMaterialSpriteProvider extends AbstractMaterialSpriteProvider {

    @Override
    public String getName() {
        return "";
    }

    @Override
    protected void addAllMaterials() {

        for(TinkersMaterial material : TinkersMaterial.MATERIALS){
            buildMaterial(material.getMaterialLocation())
                    .colorMapper(material.getColorMapping())
                    .repairKit().statType(StatlessMaterialStats.BINDING.getIdentifier());

        }

    }
}
