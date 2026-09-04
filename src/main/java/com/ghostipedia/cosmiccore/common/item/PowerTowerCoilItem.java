package com.ghostipedia.cosmiccore.common.item;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.world.item.Item;

public final class PowerTowerCoilItem extends Item {

    private final int voltageTier;

    public PowerTowerCoilItem(Properties properties, int voltageTier) {
        super(properties);
        if (voltageTier < GTValues.LV || voltageTier >= GTValues.V.length) {
            throw new IllegalArgumentException("Power Tower Coil voltage tier is outside the registered GT range");
        }
        this.voltageTier = voltageTier;
    }

    public int getVoltageTier() {
        return voltageTier;
    }
}
