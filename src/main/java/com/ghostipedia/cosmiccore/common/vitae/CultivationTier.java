package com.ghostipedia.cosmiccore.common.vitae;

import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.util.StringRepresentable;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum CultivationTier implements StringRepresentable {

    MV("mv", GTValues.MV, 250, 400, 100, 1),
    EV("ev", GTValues.EV, 500, 400, 400, 4),
    LUV("luv", GTValues.LuV, 1_000, 400, 1_600, 16);

    public static final Codec<CultivationTier> CODEC = StringRepresentable.fromEnum(CultivationTier::values);

    private final String serializedName;
    private final int voltageTier;
    private final int nutrientAmount;
    private final int duration;
    private final long bloomwyrmCharge;
    private final int yieldMultiplier;

    CultivationTier(String serializedName, int voltageTier, int nutrientAmount, int duration,
                    long bloomwyrmCharge, int yieldMultiplier) {
        this.serializedName = serializedName;
        this.voltageTier = voltageTier;
        this.nutrientAmount = nutrientAmount;
        this.duration = duration;
        this.bloomwyrmCharge = bloomwyrmCharge;
        this.yieldMultiplier = yieldMultiplier;
    }

    @Override
    public @NotNull String getSerializedName() {
        return serializedName;
    }

    public int voltageTier() {
        return voltageTier;
    }

    public long eut() {
        return GTValues.VA[voltageTier];
    }

    public int nutrientAmount() {
        return nutrientAmount;
    }

    public int duration() {
        return duration;
    }

    public long bloomwyrmCharge() {
        return bloomwyrmCharge;
    }

    public int yieldMultiplier() {
        return yieldMultiplier;
    }

    public List<Material> acceptedNutrients() {
        return switch (this) {
            case MV -> List.of(
                    CosmicMaterials.BiomeldNutrientMV,
                    CosmicMaterials.BiomeldNutrientEV,
                    CosmicMaterials.BiomeldNutrientLuV);
            case EV -> List.of(CosmicMaterials.BiomeldNutrientEV, CosmicMaterials.BiomeldNutrientLuV);
            case LUV -> List.of(CosmicMaterials.BiomeldNutrientLuV);
        };
    }
}
