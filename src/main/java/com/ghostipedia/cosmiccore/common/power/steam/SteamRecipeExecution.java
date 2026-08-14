package com.ghostipedia.cosmiccore.common.power.steam;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;

import org.jetbrains.annotations.Nullable;

public final class SteamRecipeExecution {

    public static final String PLAN_VERSION_KEY = "cosmiccore:steam_plan_version";
    public static final String TOTAL_STEAM_KEY = "cosmiccore:steam_total";
    public static final String STEAM_PER_TICK_KEY = "cosmiccore:steam_per_tick";

    private static final int PLAN_VERSION = 1;
    private static final int LOW_PRESSURE_MAX_STEAM_PER_TICK = 32;
    private static final int HIGH_PRESSURE_MAX_STEAM_PER_TICK = 64;

    private SteamRecipeExecution() {}

    public static @Nullable Plan resolve(GTRecipe recipe) {
        if (recipe.duration <= 0 || RecipeHelper.getRecipeEUtTier(recipe) > GTValues.LV) {
            return null;
        }

        EnergyStack inputEUt = recipe.getInputEUt();
        if (inputEUt.isEmpty()) {
            return null;
        }

        try {
            long authoredEUt = Math.multiplyExact(inputEUt.voltage(), inputEUt.amperage());
            long authoredWork = Math.multiplyExact(authoredEUt, (long) recipe.duration);
            long totalSteam = Math.multiplyExact(authoredWork, 2L);
            if (authoredEUt <= 0 || totalSteam <= 0) {
                return null;
            }

            long nominalEUt = Math.min(authoredEUt, GTValues.V[GTValues.LV]);
            int highPressureFlow = authoredEUt >= GTValues.V[GTValues.LV] ?
                    HIGH_PRESSURE_MAX_STEAM_PER_TICK : Math.toIntExact(Math.multiplyExact(authoredEUt, 2L));
            int lowPressureFlow = Math.toIntExact(Math.min(authoredEUt, LOW_PRESSURE_MAX_STEAM_PER_TICK));
            Profile lowPressure = profile(nominalEUt, totalSteam, lowPressureFlow);
            Profile highPressure = profile(nominalEUt, totalSteam, highPressureFlow);
            if (lowPressure == null || highPressure == null) {
                return null;
            }
            return new Plan(authoredEUt, totalSteam, lowPressure, highPressure);
        } catch (ArithmeticException ignored) {
            return null;
        }
    }

    public static @Nullable GTRecipe createRuntimeRecipe(GTRecipe recipe, boolean highPressure) {
        Plan plan = resolve(recipe);
        if (plan == null) {
            return null;
        }

        Profile profile = plan.profile(highPressure);
        GTRecipe runtimeRecipe = recipe.copy();
        runtimeRecipe.data = recipe.data.copy();
        runtimeRecipe.duration = profile.durationTicks();
        EURecipeCapability.putEUContent(runtimeRecipe.tickInputs, new EnergyStack(profile.nominalEUt()));
        runtimeRecipe.data.putInt(PLAN_VERSION_KEY, PLAN_VERSION);
        runtimeRecipe.data.putLong(TOTAL_STEAM_KEY, plan.totalSteam());
        runtimeRecipe.data.putInt(STEAM_PER_TICK_KEY, profile.steamPerTick());
        return runtimeRecipe;
    }

    public static @Nullable RuntimePlan runtimePlan(GTRecipe recipe) {
        if (recipe.data.getInt(PLAN_VERSION_KEY) != PLAN_VERSION) {
            return null;
        }

        long totalSteam = recipe.data.getLong(TOTAL_STEAM_KEY);
        int steamPerTick = recipe.data.getInt(STEAM_PER_TICK_KEY);
        if (totalSteam <= 0 || steamPerTick <= 0 || recipe.duration <= 0) {
            return null;
        }

        long duration = ceilDiv(totalSteam, steamPerTick);
        if (duration != recipe.duration) {
            return null;
        }
        return new RuntimePlan(totalSteam, steamPerTick, recipe.duration);
    }

    public static boolean hasRuntimePlanMarker(GTRecipe recipe) {
        return recipe.data.contains(PLAN_VERSION_KEY) ||
                recipe.data.contains(TOTAL_STEAM_KEY) ||
                recipe.data.contains(STEAM_PER_TICK_KEY);
    }

    private static @Nullable Profile profile(long nominalEUt, long totalSteam, int steamPerTick) {
        if (nominalEUt <= 0 || steamPerTick <= 0) {
            return null;
        }
        long duration = ceilDiv(totalSteam, steamPerTick);
        if (duration <= 0 || duration > Integer.MAX_VALUE) {
            return null;
        }
        return new Profile(nominalEUt, steamPerTick, (int) duration);
    }

    private static long ceilDiv(long dividend, long divisor) {
        return 1L + (dividend - 1L) / divisor;
    }

    public record Plan(long authoredEUt, long totalSteam, Profile lowPressure, Profile highPressure) {

        public Profile profile(boolean highPressure) {
            return highPressure ? this.highPressure : this.lowPressure;
        }
    }

    public record Profile(long nominalEUt, int steamPerTick, int durationTicks) {}

    public record RuntimePlan(long totalSteam, int steamPerTick, int durationTicks) {

        public int steamForProgress(int progress) {
            int normalizedProgress = progress < 0 || progress >= this.durationTicks ? 0 : progress;
            long consumed = (long) normalizedProgress * this.steamPerTick;
            long remaining = this.totalSteam - consumed;
            if (remaining <= 0) {
                return 0;
            }
            return Math.toIntExact(Math.min(remaining, this.steamPerTick));
        }
    }
}
