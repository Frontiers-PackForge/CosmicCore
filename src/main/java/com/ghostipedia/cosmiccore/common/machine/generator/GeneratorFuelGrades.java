package com.ghostipedia.cosmiccore.common.machine.generator;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Map;

public final class GeneratorFuelGrades {
    //Reference, LV is 1 rather than ULV
    private static final Map<ResourceLocation, Integer> GRADES = Map.ofEntries(
            Map.entry(ResourceLocation.parse("gtceu:cinder_gas"), 1),
            Map.entry(ResourceLocation.parse("gtceu:methane"), 1),
            Map.entry(ResourceLocation.parse("gtceu:natural_gas"), 1),
            Map.entry(ResourceLocation.parse("gtceu:wood_gas"), 1),
            Map.entry(ResourceLocation.parse("cosmiccore:high_pressure_steam"), 1),
            Map.entry(ResourceLocation.parse("gtceu:steam"), 1),
            Map.entry(ResourceLocation.parse("gtceu:ethanol"), 1),
            Map.entry(ResourceLocation.parse("gtceu:light_oil"), 1),
            Map.entry(ResourceLocation.parse("gtceu:methanol"), 1),
            Map.entry(ResourceLocation.parse("gtceu:turpentine"), 1),
            Map.entry(ResourceLocation.parse("cosmiccore:crude_benzene"), 2),
            Map.entry(ResourceLocation.parse("gtceu:ethylene"), 2),
            Map.entry(ResourceLocation.parse("gtceu:phenol"), 2),
            Map.entry(ResourceLocation.parse("gtceu:refinery_gas"), 2),
            Map.entry(ResourceLocation.parse("cosmiccore:tail_gas"), 2),
            Map.entry(ResourceLocation.parse("gtceu:smoldering_steam"), 2),
            Map.entry(ResourceLocation.parse("gtceu:bio_diesel"), 2),
            Map.entry(ResourceLocation.parse("gtceu:middle_fraction_distillates"), 2),
            Map.entry(ResourceLocation.parse("gtceu:source_enhanced_bio_fuel"), 2),
            Map.entry(ResourceLocation.parse("gtceu:toluene"), 2),
            Map.entry(ResourceLocation.parse("gtceu:awakened_cinder_gas"), 2),
            Map.entry(ResourceLocation.parse("gtceu:ethane"), 3),
            Map.entry(ResourceLocation.parse("gtceu:lpg"), 3),
            Map.entry(ResourceLocation.parse("gtceu:propane"), 3),
            Map.entry(ResourceLocation.parse("gtceu:propene"), 3),
            Map.entry(ResourceLocation.parse("gtceu:super_critical_steam"), 3),
            Map.entry(ResourceLocation.parse("gtceu:cetane_boosted_diesel"), 3),
            Map.entry(ResourceLocation.parse("gtceu:diesel"), 3),
            Map.entry(ResourceLocation.parse("gtceu:octane"), 3),
            Map.entry(ResourceLocation.parse("gtceu:thunder_boosted_source_diesel"), 3),
            Map.entry(ResourceLocation.parse("gtceu:butadiene"), 3),
            Map.entry(ResourceLocation.parse("gtceu:butane"), 3),
            Map.entry(ResourceLocation.parse("gtceu:butene"), 3),
            Map.entry(ResourceLocation.parse("gtceu:ashdew_steam"), 4),
            Map.entry(ResourceLocation.parse("gtceu:gasoline"), 4),
            Map.entry(ResourceLocation.parse("gtceu:high_octane_gasoline"), 4),
            Map.entry(ResourceLocation.parse("gtceu:rocket_fuel"), 4));

    private GeneratorFuelGrades() {}

    public static Map<ResourceLocation, Integer> grades() {
        return GRADES;
    }

    public static int grade(ResourceLocation fluid) {
        return GRADES.getOrDefault(fluid, 0);
    }

    public static int grade(FluidStack fluid) {
        return fluid.isEmpty() ? 0 : grade(BuiltInRegistries.FLUID.getKey(fluid.getFluid()));
    }

    public static int grade(GTRecipe recipe) {
        var inputs = recipe.getInputContents(FluidRecipeCapability.CAP);
        if (inputs.size() != 1) return 0;
        var stacks = FluidRecipeCapability.CAP.of(inputs.getFirst().content()).getFluids();
        int grade = 0;
        for (var stack : stacks) {
            int candidate = grade(stack);
            if (candidate == 0 || (grade != 0 && candidate != grade)) return 0;
            grade = candidate;
        }
        return grade;
    }

    public static boolean isSingleblockRecipeType(ResourceLocation type) {
        return type.getNamespace().equals("gtceu") && switch (type.getPath()) {
            case "steam_turbine", "gas_turbine", "combustion_generator" -> true;
            default -> false;
        };
    }

    public static boolean isGradedRecipeType(ResourceLocation type) {
        return isSingleblockRecipeType(type) || type.getNamespace().equals("cosmiccore") &&
                (type.getPath().equals("turbine_power_station") || type.getPath().equals("combustion_power_station"));
    }

    public static OppScaleThingyWhatDoICallThisItsTheFuelDensityLol operatingPoint(int grade, int generatorTier, long baseAmperage, long hardwareLimit) {
        if (grade < GTValues.LV || grade > GTValues.EV || generatorTier < GTValues.LV ||
                generatorTier >= GTValues.V.length || baseAmperage <= 0 || hardwareLimit <= 0 ||
                generatorTier - grade >= 2) {
            return new OppScaleThingyWhatDoICallThisItsTheFuelDensityLol(0, 0);
        }
        long fuelVoltage = GTValues.V[Math.min(grade, generatorTier)];
        long ceiling = baseAmperage > Long.MAX_VALUE / fuelVoltage ? Long.MAX_VALUE : baseAmperage * fuelVoltage;
        return new OppScaleThingyWhatDoICallThisItsTheFuelDensityLol(Math.min(ceiling, hardwareLimit), generatorTier > grade ? 0.5 : 1.0);
    }

    public static ModifierFunction modifier(MetaMachine machine, GTRecipe recipe, int generatorTier,
                                            long baseAmperage, long hardwareLimit, double outputFraction) {
        int grade = grade(recipe);
        if (grade == 0) return reject(machine, recipe, Component.translatable("cosmiccore.fuel_grade.unassigned"));
        OppScaleThingyWhatDoICallThisItsTheFuelDensityLol point = operatingPoint(grade, generatorTier, baseAmperage, hardwareLimit);
        if (generatorTier - grade >= 2) {
            return reject(machine, recipe, Component.translatable("cosmiccore.fuel_grade.incompatible",
                    grade, GTValues.VN[grade + 1]));
        }
        if (point.maximumEUt() <= 0 || outputFraction <= 0 || outputFraction > 1) return ModifierFunction.NULL;
        GTRecipe fuelRecipe = point.efficiency() == 1 ? recipe :
                ModifierFunction.builder().eutMultiplier(point.efficiency()).build().apply(recipe);
        long baseOutput = fuelRecipe.getOutputEUt().getTotalEU();
        if (baseOutput <= 0) return ModifierFunction.NULL;
        int maximumParallel = (int) Math.min(Integer.MAX_VALUE, point.maximumEUt() / baseOutput);
        if (maximumParallel < 1) return ModifierFunction.NULL;
        int parallel = ParallelLogic.getParallelAmountFast(machine, fuelRecipe, maximumParallel);
        if (parallel < 1 || (long) (fuelRecipe.getOutputEUt().voltage() * parallel * outputFraction) < 1) {
            return reject(machine, recipe, Component.translatable("cosmiccore.fuel_grade.insufficient_batch"));
        }
        return ModifierFunction.builder()
                .inputModifier(ContentModifier.multiplier(parallel))
                .outputModifier(ContentModifier.multiplier(parallel))
                .eutMultiplier(parallel * point.efficiency() * outputFraction)
                .durationMultiplier(1.0 / outputFraction)
                .parallels(parallel)
                .build();
    }

    private static ModifierFunction reject(MetaMachine machine, GTRecipe recipe, Component reason) {
        RecipeLogic.putFailureReason(machine, recipe, reason);
        return ModifierFunction.cancel(reason);
    }

    public record OppScaleThingyWhatDoICallThisItsTheFuelDensityLol(long maximumEUt, double efficiency) {}
}
