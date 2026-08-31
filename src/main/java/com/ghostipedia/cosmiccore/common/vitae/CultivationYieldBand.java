package com.ghostipedia.cosmiccore.common.vitae;

import net.minecraft.util.StringRepresentable;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;

public enum CultivationYieldBand implements StringRepresentable {

    NONE("none", 0),
    TRACE("trace", 50),
    LOW("low", 125),
    STANDARD("standard", 250),
    HIGH("high", 500),
    EXCEPTIONAL("exceptional", 1_000);

    public static final Codec<CultivationYieldBand> CODEC = StringRepresentable.fromEnum(
            CultivationYieldBand::values);

    private final String serializedName;
    private final int baseUnits;

    CultivationYieldBand(String serializedName, int baseUnits) {
        this.serializedName = serializedName;
        this.baseUnits = baseUnits;
    }

    @Override
    public @NotNull String getSerializedName() {
        return serializedName;
    }

    public int units(CultivationTier tier) {
        return Math.multiplyExact(baseUnits, tier.yieldMultiplier());
    }
}
