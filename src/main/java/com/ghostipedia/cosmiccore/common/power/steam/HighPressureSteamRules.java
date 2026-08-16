package com.ghostipedia.cosmiccore.common.power.steam;

import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;

import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.neoforged.neoforge.fluids.FluidStack;

public final class HighPressureSteamRules {

    public static final int STEAM_COMPRESSION_RATIO = 8;

    private HighPressureSteamRules() {}

    public static boolean isHighPressureSteam(FluidStack stack) {
        return !stack.isEmpty() && stack.getFluid() == CosmicMaterials.HighPressureSteam.getFluid();
    }

    public static FluidStack highPressureSteam(int amount) {
        return CosmicMaterials.HighPressureSteam.getFluid(amount);
    }

    public static FluidStack expandedSteam(int highPressureSteamAmount) {
        return GTMaterials.Steam.getFluid(Math.multiplyExact(highPressureSteamAmount, STEAM_COMPRESSION_RATIO));
    }
}
