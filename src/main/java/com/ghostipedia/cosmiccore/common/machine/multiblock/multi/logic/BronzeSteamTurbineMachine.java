package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;
import com.ghostipedia.cosmiccore.common.power.steam.BronzeSteamTurbinePolicy;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.multiblock.part.RotorHolderPartMachine;
import com.gregtechceu.gtceu.common.mui.GTMultiblockTextUtil;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BronzeSteamTurbineMachine extends WorkableElectricMultiblockMachine implements ITieredMachine {

    public BronzeSteamTurbineMachine(BlockEntityCreationInfo info) {
        super(info);
        recipeLogic.setRegressWhenWaiting(false);
    }

    @Override
    public int getTier() {
        return GTValues.LV;
    }

    @Nullable
    private RotorHolderPartMachine getRotorHolder() {
        for (MultiblockPartMachine part : getParts()) {
            if (part instanceof RotorHolderPartMachine rotorHolder) return rotorHolder;
        }
        return null;
    }

    public boolean hasRotor() {
        RotorHolderPartMachine rotorHolder = getRotorHolder();
        return rotorHolder != null && rotorHolder.hasRotor();
    }

    public int getRotorSpeed() {
        RotorHolderPartMachine rotorHolder = getRotorHolder();
        return rotorHolder != null && rotorHolder.hasRotor() ? rotorHolder.getRotorSpeed() : 0;
    }

    public int getMaximumRotorSpeed() {
        RotorHolderPartMachine rotorHolder = getRotorHolder();
        return rotorHolder != null && rotorHolder.hasRotor() ? rotorHolder.getMaxRotorHolderSpeed() : 0;
    }

    public double getCombinedEfficiency() {
        RotorHolderPartMachine rotorHolder = getRotorHolder();
        if (rotorHolder == null || !rotorHolder.hasRotor()) return 0;
        return BronzeSteamTurbinePolicy.combinedEfficiency(rotorHolder.getTier(), rotorHolder.getRotorEfficiency());
    }

    public long getMechanicalProduction() {
        RotorHolderPartMachine rotorHolder = getRotorHolder();
        if (rotorHolder == null || !rotorHolder.hasRotor()) return 0;
        return BronzeSteamTurbinePolicy.mechanicalOutput(rotorHolder.getTier(), rotorHolder.getRotorPower());
    }

    public long getMaximumProduction() {
        RotorHolderPartMachine rotorHolder = getRotorHolder();
        if (rotorHolder == null || !rotorHolder.hasRotor()) return 0;
        return BronzeSteamTurbinePolicy.ratedOutput(
                rotorHolder.getTier(), rotorHolder.getRotorPower(), getDisplayGeneratorPower());
    }

    @Override
    public long getOverclockVoltage() {
        return getMaximumProduction();
    }

    public long getCurrentProduction() {
        GTRecipe recipe = recipeLogic.getLastUnrolledRecipe();
        return isActive() && recipe != null ? recipe.getOutputEUt().getTotalEU() : 0;
    }

    private double productionBoost() {
        int maximumSpeed = getMaximumRotorSpeed();
        if (maximumSpeed <= 0) return 0;
        int currentSpeed = getRotorSpeed();
        if (currentSpeed >= maximumSpeed) return 1;
        return Math.pow((double) currentSpeed / maximumSpeed, 2);
    }

    private long getFuelRateHundredths() {
        GTRecipe recipe = recipeLogic.getLastUnrolledRecipe();
        if (!isActive() || recipe == null || recipe.duration <= 0) return 0;
        long amount = 0;
        for (var content : recipe.getInputContents(FluidRecipeCapability.CAP)) {
            FluidStack[] fluids = FluidRecipeCapability.CAP.of(content.content()).getFluids();
            if (fluids.length == 1) amount += fluids[0].getAmount();
        }
        return Math.round(amount * 100.0 / recipe.duration);
    }

    private static boolean acceptsFuel(GTRecipe recipe) {
        boolean found = false;
        for (var content : recipe.getInputContents(FluidRecipeCapability.CAP)) {
            FluidStack[] fluids = FluidRecipeCapability.CAP.of(content.content()).getFluids();
            if (fluids.length != 1) return false;
            found = true;
            if (fluids[0].getFluid() != GTMaterials.Steam.getFluid() &&
                    fluids[0].getFluid() != CosmicMaterials.HighPressureSteam.getFluid())
                return false;
        }
        return found;
    }

    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof BronzeSteamTurbineMachine turbine)) {
            return RecipeModifier.nullWrongType(BronzeSteamTurbineMachine.class, machine);
        }
        RotorHolderPartMachine rotorHolder = turbine.getRotorHolder();
        if (rotorHolder == null || !rotorHolder.hasRotor() || !acceptsFuel(recipe)) return ModifierFunction.NULL;
        EnergyStack output = recipe.getOutputEUt();
        long recipeOutput = output.getTotalEU();
        long ratedOutput = turbine.getMaximumProduction();
        double efficiency = turbine.getCombinedEfficiency();
        if (output.isEmpty() || ratedOutput < recipeOutput || efficiency <= 0) return ModifierFunction.NULL;
        int requiredParallels = BronzeSteamTurbinePolicy.requiredParallels(ratedOutput, recipeOutput);
        int actualParallels = ParallelLogic.getParallelAmountFast(turbine, recipe, requiredParallels);
        if (actualParallels <= 0) return ModifierFunction.NULL;
        double outputMultiplier = turbine.productionBoost() * BronzeSteamTurbinePolicy.outputMultiplier(
                ratedOutput, recipeOutput, requiredParallels, actualParallels);
        double durationMultiplier = BronzeSteamTurbinePolicy.durationMultiplier(
                efficiency, ratedOutput, recipeOutput, requiredParallels, actualParallels);
        return ModifierFunction.builder()
                .inputModifier(ContentModifier.multiplier(actualParallels))
                .outputModifier(ContentModifier.multiplier(actualParallels))
                .eutMultiplier(outputMultiplier)
                .parallels(actualParallels)
                .durationMultiplier(durationMultiplier)
                .build();
    }

    @Override
    public boolean canVoidRecipeOutputs(RecipeCapability<?> capability) {
        return true;
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = new ArrayList<>();
        widgets.add(GTMultiblockTextUtil.addEnergyTierLine(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addEnergyUsageLine(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addUnformedWarning(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addProgressLine(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addWorkingStatusLine(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addRecipeTypeField(this, syncManager));
        widgets.addAll(getDefinition().getAdditionalDisplay().apply(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addBatchModeLine(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addTotalRunsLine(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addOutputLines(this, syncManager));
        widgets.addAll(GTMultiblockTextUtil.addRecipeFailReasonLines(this, syncManager));
        IntSyncValue speed = new IntSyncValue(this::getRotorSpeed);
        IntSyncValue maximumSpeed = new IntSyncValue(this::getMaximumRotorSpeed);
        IntSyncValue efficiency = new IntSyncValue(() -> (int) Math.round(getCombinedEfficiency() * 10_000));
        LongSyncValue currentProduction = new LongSyncValue(this::getCurrentProduction);
        LongSyncValue maximumProduction = new LongSyncValue(this::getMaximumProduction);
        LongSyncValue fuelRate = new LongSyncValue(this::getFuelRateHundredths);
        syncManager.syncValue("bronze_turbine_speed", speed);
        syncManager.syncValue("bronze_turbine_maximum_speed", maximumSpeed);
        syncManager.syncValue("bronze_turbine_efficiency", efficiency);
        syncManager.syncValue("bronze_turbine_current_production", currentProduction);
        syncManager.syncValue("bronze_turbine_maximum_production", maximumProduction);
        syncManager.syncValue("bronze_turbine_fuel_rate", fuelRate);
        widgets.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.multiblock.bronze_steam_turbine.rotor_speed",
                FormattingUtil.formatNumbers(speed.getIntValue()),
                FormattingUtil.formatNumbers(maximumSpeed.getIntValue())).withStyle(ChatFormatting.AQUA)).asWidget());
        widgets.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.multiblock.bronze_steam_turbine.efficiency",
                FormattingUtil.formatNumber2Places(efficiency.getIntValue() / 100.0)).withStyle(ChatFormatting.AQUA))
                .asWidget());
        widgets.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.multiblock.bronze_steam_turbine.production",
                FormattingUtil.formatNumbers(currentProduction.getLongValue()),
                FormattingUtil.formatNumbers(maximumProduction.getLongValue())).withStyle(ChatFormatting.YELLOW))
                .asWidget());
        widgets.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.multiblock.bronze_steam_turbine.fuel_rate",
                FormattingUtil.formatNumber2Places(fuelRate.getLongValue() / 100.0)).withStyle(ChatFormatting.GOLD))
                .asWidget());
        return widgets;
    }
}
