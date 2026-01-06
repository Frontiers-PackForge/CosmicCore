package com.ghostipedia.cosmiccore.api.data.material.property;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.IMaterialProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.MaterialProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;

import lombok.Getter;

public class FluidTooltipProperty implements IMaterialProperty {

    @Getter
    private String key;
    @Getter
    private double value;

    public FluidTooltipProperty(String prefix, double value) {
        this.key = prefix;
        this.value = value;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.FLUID, true);
    }
}
