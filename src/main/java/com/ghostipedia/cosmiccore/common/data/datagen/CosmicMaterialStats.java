package com.ghostipedia.cosmiccore.common.data.datagen;

import com.ghostipedia.cosmiccore.common.data.materials.tinkers.TinkersMaterial;
import net.minecraft.data.PackOutput;
import slimeknights.tconstruct.library.data.material.AbstractMaterialDataProvider;
import slimeknights.tconstruct.library.data.material.AbstractMaterialStatsDataProvider;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;

import java.util.List;

public class CosmicMaterialStats extends AbstractMaterialStatsDataProvider {
    public CosmicMaterialStats(PackOutput packOutput, AbstractMaterialDataProvider materials) {
        super(packOutput, materials);
    }

    @Override
    protected void addMaterialStats() {

        for (TinkersMaterial material : TinkersMaterial.MATERIALS){

            addMaterialStats(material.getMaterialLocation(), material.getStats().toArray(new IMaterialStats[0]));

        }

    }

    @Override
    public String getName() {
        return "cosmic MaterialStats";
    }
}
