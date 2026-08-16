package com.ghostipedia.cosmiccore.common.power.steam;

import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;

import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.neoforged.neoforge.fluids.FluidStack;

public final class HPBoilerRates {

    public static final int COMPACT_RATE = 8;

    private HPBoilerRates() {}

    public static boolean isHighPressureSteam(FluidStack stack) {
        return !stack.isEmpty() && stack.getFluid() == CosmicMaterials.HighPressureSteam.getFluid();
    }

    public static FluidStack highPressureSteam(int amount) {
        return CosmicMaterials.HighPressureSteam.getFluid(amount);
    }

    public static FluidStack expandedSteam(int highPressureSteamAmount) {
        return GTMaterials.Steam.getFluid(Math.multiplyExact(highPressureSteamAmount, COMPACT_RATE));
    }
}
