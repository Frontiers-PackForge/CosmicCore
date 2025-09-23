package com.ghostipedia.cosmiccore.common.data.datagen;

import com.ghostipedia.cosmiccore.CosmicCore;

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
        // todo figure this out
    }

    @Override
    public String getName() {
        return "";
    }
}
