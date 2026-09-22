package com.ghostipedia.cosmiccore.common.machine.foundry;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

public final class FoundryPyrofluxPolicy {

    public static final int LINK_RANGE = 96;
    public static final ResourceLocation FLUID_ID = ResourceLocation.fromNamespaceAndPath("gtceu", "pyroflux");
    public static final int GENERATION_DURATION = 50;
    public static final int LIQUOR_PER_BATCH = 1000;
    public static final int STONE_DUST_PER_BATCH = 1;
    public static final long GENERATION_EUT = GTValues.V[GTValues.HV];
    public static final long CHARGE_PER_BATCH = 16_000;
    public static final long MAX_CHARGE_PER_RECIPE = 1000;
    public static final long EU_PER_CHARGE = 256;
    public static final int HEAT_STEP = 1800;
    private static final long BASE_CAPACITY = 32_768;

    private FoundryPyrofluxPolicy() {}

    public static Fluid fluid() {
        return BuiltInRegistries.FLUID.get(FLUID_ID);
    }

    public static int generationParallel(FoundryTier tier, long voltage) {
        int voltageTier = GTUtil.getFloorTierByVoltage(Math.max(0, voltage));
        if (voltageTier < GTValues.HV) return 0;
        int voltageParallel = 1 << Math.min(30, voltageTier - GTValues.HV);
        int tierParallel = 1 << (tier.level() - 1);
        return Math.min(voltageParallel, tierParallel);
    }

    public static long capacity(FoundryTier tier) {
        return BASE_CAPACITY << (tier.level() - 1);
    }

    public static long demand(GTRecipe recipe) {
        int requiredHeat = recipe.data.contains("ebf_temp") ? recipe.data.getInt("ebf_temp") : HEAT_STEP;
        return cappedDemand(recipe.getInputEUt().getTotalEU(), recipe.duration, requiredHeat, recipe.parallels);
    }

    static long cappedDemand(long totalEUt, int duration, int requiredHeat, int parallels) {
        int safeParallels = Math.max(1, parallels);
        long perRecipeEUt = ceilDiv(saturatingAbs(totalEUt), safeParallels);
        long perRecipeDemand = Math.min(MAX_CHARGE_PER_RECIPE,
                demand(perRecipeEUt, duration, requiredHeat));
        return saturatingMultiply(perRecipeDemand, safeParallels);
    }

    public static long demand(long eut, int duration, int requiredHeat) {
        long safeEUt = saturatingAbs(eut);
        long safeDuration = Math.max(1, duration);
        long heatMultiplier = Math.max(1, ceilDiv(Math.max(0, requiredHeat), HEAT_STEP));
        return ceilDiv(saturatingMultiply(saturatingMultiply(safeEUt, safeDuration), heatMultiplier),
                EU_PER_CHARGE);
    }

    public static long consumedAtProgress(long totalDemand, int progress, int duration) {
        if (totalDemand <= 0 || progress <= 0) return 0;
        if (progress >= duration) return totalDemand;
        return ceilDiv(saturatingMultiply(totalDemand, progress), Math.max(1, duration));
    }

    public static long chargeForNextTick(long totalDemand, int progress, int duration) {
        long before = consumedAtProgress(totalDemand, progress, duration);
        long after = consumedAtProgress(totalDemand, progress + 1, duration);
        return Math.max(0, after - before);
    }

    public static int recipeHeat(GTRecipe recipe) {
        return recipe.data.contains("ebf_temp") ? recipe.data.getInt("ebf_temp") : 0;
    }

    public static int maximumTemperature(int coilTemperature, int machineTier) {
        return coilTemperature + 100 * Math.max(0, machineTier - GTValues.MV);
    }

    private static long ceilDiv(long value, long divisor) {
        if (value <= 0) return 0;
        return 1 + (value - 1) / divisor;
    }

    private static long saturatingMultiply(long left, long right) {
        if (left <= 0 || right <= 0) return 0;
        if (left > Long.MAX_VALUE / right) return Long.MAX_VALUE;
        return left * right;
    }

    private static long saturatingAbs(long value) {
        return value == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(value);
    }
}
