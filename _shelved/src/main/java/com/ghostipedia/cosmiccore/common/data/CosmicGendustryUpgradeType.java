package com.ghostipedia.cosmiccore.common.data;

import forestry.api.core.IItemSubtype;
import thedarkcolour.gendustry.item.IGendustryUpgradeType;

import java.util.Locale;

public enum CosmicGendustryUpgradeType implements IItemSubtype, IGendustryUpgradeType {

    WAILING(1, 2048),
    DECAYING(1, 2048);

    private final String name;
    private final int maxStackSize;
    private final int energyCost;

    private CosmicGendustryUpgradeType(int maxStackSize, int energyCost) {
        this.name = this.name().toLowerCase(Locale.ENGLISH);
        this.maxStackSize = maxStackSize;
        this.energyCost = energyCost;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public int maxStackSize() {
        return this.maxStackSize;
    }

    public int energyCost() {
        return this.energyCost;
    }
}
