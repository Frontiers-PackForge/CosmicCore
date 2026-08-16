package com.ghostipedia.cosmiccore.common.power.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.nbt.Tag;

import java.util.List;

public final class PowerRecipeVoltageConverter {
    // The goal of this class is to handle extracting the original EU/t and throwing it to the wolves
    // The voltage is captured, then converted into target EU/t and Amp values.
    // This does, however have the unintended side-effect of raising the general cost of everything that was <LV Amps before.
    // This is used in tandem with GTRecipeVoltageStripper to kill off any trace EUt information that isn't relevant.
    public static final String POLICY_VERSION_KEY = "cosmiccore:power_workload_version";
    public static final String ORIGINAL_EUT_KEY = "cosmiccore:power_workload_original_eut";
    public static final String TARGET_AMPS_KEY = "cosmiccore:power_workload_target_amps";

    private static final int POLICY_VERSION = 1;

    private PowerRecipeVoltageConverter() {}

    public static boolean apply(GTRecipe recipe) {
        if (recipe.id == null || !GTCEu.MOD_ID.equals(recipe.id.getNamespace()) || recipe.duration <= 0 ||
                recipe.data.contains(POLICY_VERSION_KEY, Tag.TAG_INT)) {
            return false;
        }

        List<Content> energyContents = recipe.tickInputs.get(EURecipeCapability.CAP);
        if (energyContents == null || energyContents.size() != 1) {
            return false;
        }
        //
        EnergyStack originalVal = EURecipeCapability.CAP.of(energyContents.getFirst().content());
        if (originalVal.isEmpty()) {
            return false;
        }

        try {
            long adjustedEUT = Math.multiplyExact(originalVal.voltage(), originalVal.amperage());
            int tier = GTUtil.getTierByVoltage(originalVal.voltage());
            long targetVoltz = GTValues.V[tier];
            double amperageAdjustment = (double) adjustedEUT / targetVoltz * recipe.duration;
            int targetAmps = PowerRecipeWorkloadCurves.targetAmperage(recipe.recipeType.registryName.getPath(),
                    amperageAdjustment);
            if (targetAmps <= 0) {
                return false;
            }

            Math.multiplyExact(targetVoltz, targetAmps);
            EURecipeCapability.putEUContent(recipe.tickInputs, new EnergyStack(targetVoltz, targetAmps));
            recipe.data.putInt(POLICY_VERSION_KEY, POLICY_VERSION);
            recipe.data.putLong(ORIGINAL_EUT_KEY, adjustedEUT);
            recipe.data.putInt(TARGET_AMPS_KEY, targetAmps);
            return true;
        } catch (ArithmeticException ignored) {
            return false;
        }
    }
}
