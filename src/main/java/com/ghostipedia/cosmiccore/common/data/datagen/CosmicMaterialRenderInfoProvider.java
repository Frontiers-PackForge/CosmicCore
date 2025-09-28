package com.ghostipedia.cosmiccore.common.data.datagen;

import com.ghostipedia.cosmiccore.common.data.materials.tinkers.TinkersMaterial;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;

import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.client.data.material.AbstractMaterialRenderInfoProvider;
import slimeknights.tconstruct.library.client.data.material.AbstractMaterialSpriteProvider;

public class CosmicMaterialRenderInfoProvider extends AbstractMaterialRenderInfoProvider {

    public CosmicMaterialRenderInfoProvider(PackOutput packOutput,
                                            @Nullable AbstractMaterialSpriteProvider materialSprites,
                                            @Nullable ExistingFileHelper existingFileHelper) {
        super(packOutput, materialSprites, existingFileHelper);
    }

    @Override
    protected void addMaterialRenderInfo() {
        for (TinkersMaterial material : TinkersMaterial.MATERIALS) {
            buildRenderInfo(material.getMaterialLocation())
                    .color(material.getColor())
                    .fallbacks(material.getFallbacks().toArray(new String[0]));

        }
    }

    @Override
    public String getName() {
        return "cosmic material render provider";
    }
}
