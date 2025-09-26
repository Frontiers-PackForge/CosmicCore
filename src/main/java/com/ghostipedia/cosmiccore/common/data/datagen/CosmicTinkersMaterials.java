package com.ghostipedia.cosmiccore.common.data.datagen;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.materials.tinkers.TinkersMaterial;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import slimeknights.tconstruct.library.data.material.AbstractMaterialDataProvider;

public class CosmicTinkersMaterials extends AbstractMaterialDataProvider {

    public static final ResourceLocation SCREWDRIVERIUM = CosmicCore.id("neutronite");

    public CosmicTinkersMaterials(PackOutput out) {
        super(out);
    }

    @Override
    protected void addMaterials() {
        for (TinkersMaterial material : TinkersMaterial.MATERIALS) {
            addMaterial(material.getMaterialLocation(), material.getTier(), material.getSortOrder(),
                    material.isCraftable());
        }
    }

    @Override
    public String getName() {
        return "Cosmic Core Materials";
    }
}
