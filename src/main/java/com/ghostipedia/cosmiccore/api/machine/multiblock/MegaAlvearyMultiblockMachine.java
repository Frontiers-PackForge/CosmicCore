package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.machine.multiblock.part.BeeHolderPartMachine;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import forestry.api.ForestryCapabilities;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.genetics.alleles.BeeChromosomes;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MegaAlvearyMultiblockMachine extends WorkableElectricMultiblockMachine {

    @Getter
    private List<BeeHolderPartMachine> beeHolders;

    public MegaAlvearyMultiblockMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        this.beeHolders = new ArrayList<>();
        for (IMultiPart part : getParts()) {
            if (part instanceof BeeHolderPartMachine beeHolder) {
                beeHolders.add(beeHolder);
            }
        }
    }

    @Override
    public void onStructureInvalid() {
        for (IMultiPart part : getParts()) {
            if (part instanceof BeeHolderPartMachine beeHolder) {
                beeHolder.setLocked(false);
            }
        }
        beeHolders = null;
        super.onStructureInvalid();
    }

    private void setHoldersLocked(boolean locked) {
        beeHolders.forEach(holder -> holder.setLocked(locked));
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new MegaAlvearyRecipeLogic(this);
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        setHoldersLocked(true);
        return super.beforeWorking(recipe);
    }

    @Override
    public void afterWorking() {
        setHoldersLocked(false);
        this.recipeLogic.markLastRecipeDirty();
        super.afterWorking();
    }

    public static class MegaAlvearyRecipeLogic extends RecipeLogic {

        public MegaAlvearyRecipeLogic(MegaAlvearyMultiblockMachine machine) {
            super(machine);
        }

        // Constant now, maybe change to be dependent on bee amount, etc.
        private int productivityFluidConsumptionAmount = 1000;

        private Map<Fluid, Float> productivityFluids = null;

        // Lazy getter for productivityFluids so we don't ever have issues with registration order etc.
        private Map<Fluid, Float> getProductivityFluids() {
            if (productivityFluids == null) {
                // To update the productivity values, edit these below.
                productivityFluids = new Object2FloatOpenHashMap<>();
                productivityFluids.put(Fluids.WATER, 1.5f);
            }
            return productivityFluids;
        }

        // Constant now, maybe change to be dependent on bee amount, etc.
        private int overclockFluidConsumptionAmount = 1000;

        private Map<Fluid, Float> overclockFluids = null;

        // Lazy getter for overclockFluids so we don't ever have issues with registration order etc.
        private Map<Fluid, Float> getOverclockFluids() {
            if (overclockFluids == null) {
                // To update the overclock values, edit these below.
                overclockFluids = new Object2FloatOpenHashMap<>();
                // These are multipliers, so 0.7f would reduce the recipe time by 30%
                overclockFluids.put(Fluids.LAVA, 0.7f);

            }
            return overclockFluids;
        }

        @Override
        public @NotNull Iterator<GTRecipe> searchRecipe() {
            if (!(machine instanceof MegaAlvearyMultiblockMachine)) {
                CosmicCore.LOGGER
                        .error("MegaAlvearyRecipeLogic should only be ran in the MegaAlvearyMultiblockMachine");
            }
            var builder = GTRecipeBuilder
                    .of(CosmicCore.id("bee_recipe"), CosmicRecipeTypes.BEES);

            // ====== Get and process Productivity Fluids =====
            var productivityFluids = getProductivityFluids();
            float productivityMultiplier = 1f;
            FluidStack productivityFluid = null;
            var fluidHandlers = machine.getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP);
            for (var handler : fluidHandlers) {
                if (!(handler instanceof NotifiableFluidTank fluidHandler)) continue;
                for (var content : fluidHandler.getContents()) {
                    if (!(content instanceof FluidStack stack)) continue;

                    // See if this fluid is a Productivity Fluid
                    float potentialMult = productivityFluids.getOrDefault(stack.getFluid(), -1f);

                    // If we don't have a fluid, or this isn't the highest mult, continue.
                    if (potentialMult == -1f || potentialMult < productivityMultiplier) continue;

                    // See if we have enough fluid in this handler
                    int drained = fluidHandler
                            .drainInternal(new FluidStack(stack.getFluid(), productivityFluidConsumptionAmount),
                                    IFluidHandler.FluidAction.SIMULATE)
                            .getAmount();
                    if (drained == productivityFluidConsumptionAmount) {
                        productivityFluid = new FluidStack(stack.getFluid(), productivityFluidConsumptionAmount);
                        productivityMultiplier = potentialMult;
                    }
                }
            }
            if (productivityFluid != null) {
                // This drains in 1 go at the start. To drain per tick, add .perTick(true).....perTick(false)
                builder.inputFluids(productivityFluid);
            }

            // ====== Get and process Overclock Fluids =====
            var overclockFluids = getOverclockFluids();
            float overclockMultiplier = 1f;
            FluidStack overclockFluid = null;
            for (var handler : fluidHandlers) {
                if (!(handler instanceof NotifiableFluidTank fluidHandler)) continue;
                for (var content : fluidHandler.getContents()) {
                    if (!(content instanceof FluidStack stack)) continue;

                    // See if this fluid is a Productivity Fluid
                    float potentialMult = overclockFluids.getOrDefault(stack.getFluid(), -1f);

                    // If we don't have a fluid, or this isn't the lowest OC, continue.
                    if (potentialMult == -1f || potentialMult > overclockMultiplier) continue;

                    // See if we have enough fluid in this handler
                    int drained = fluidHandler
                            .drainInternal(new FluidStack(stack.getFluid(), overclockFluidConsumptionAmount),
                                    IFluidHandler.FluidAction.SIMULATE)
                            .getAmount();
                    if (drained == overclockFluidConsumptionAmount) {
                        overclockFluid = new FluidStack(stack.getFluid(), overclockFluidConsumptionAmount);
                        overclockMultiplier = potentialMult;
                    }
                }
            }
            if (overclockFluid != null) {
                // This drains in 1 go at the start. To drain per tick, add .perTick(true).....perTick(false)
                builder.inputFluids(overclockFluid);
            }
            builder.duration((int) (5 * 20 * overclockMultiplier));

            // ====== Get and process Bees from holders =====
            var alveary = (MegaAlvearyMultiblockMachine) machine;
            int totalbees = 0;
            Map<IBeeSpecies, Integer> beeCounter = new HashMap<>();
            for (var holder : alveary.getBeeHolders()) {
                for (var content : holder.getHeldBees().getContents()) {
                    if (!(content instanceof ItemStack stack)) continue;

                    // Check if it's a Forestry Handler Item
                    var optionalCap = stack.getCapability(ForestryCapabilities.INDIVIDUAL_HANDLER_ITEM,
                            (Direction) null);
                    if (!optionalCap.isPresent()) continue;
                    var cap = optionalCap.resolve().get();

                    // Check if it's a bee
                    var individual = cap.getIndividual();
                    if (!(individual instanceof IBee bee)) continue;
                    var genome = bee.getGenome();

                    IBeeSpecies primary = genome.getActiveValue(BeeChromosomes.SPECIES);

                    beeCounter.put(primary, beeCounter.getOrDefault(primary, 0) + 1);
                    totalbees += 1;
                }
            }

            // ===== Add outputs from bee species counter =====
            for (var beeEntry : beeCounter.entrySet()) {
                for (var product : beeEntry.getKey().getProducts()) {
                    builder.chancedOutput(
                            new ItemStack(
                                    product.item(),
                                    (int) (256 * productivityMultiplier * beeEntry.getValue())),
                            (int) (product.chance() * ChanceLogic.getMaxChancedValue()),
                            0);
                }
            }

            builder.EUt(totalbees * GTValues.V[GTValues.ZPM]);
            if (totalbees == 0) {
                return Collections.emptyIterator();
            } else {
                return Collections.singleton(builder.buildRawRecipe()).iterator();
            }
        }
    }
}

// Machine logic rough draft

// Lock Bee Holders to avoid duping/false caching bees
// Collect ALl bees into a list/map
// Deririve all products and the base yield from all bees contained in holders
// H.O.N.E.Y Runs on flat 5second intervals, No Overclocks
// Can insert fluid to 'boost' yield, similar to how we do it for our Custom ExoticCombustionEngineMachine
// Will consume X Nutrients at the start of the 5s cycle to 'boost' the yield, make sure this is extensible to
// like 4 or 5 fluids for now
// Runs 5 seconds
// Ejects all Products

// More Info

// Ignore all stats besides species, we'll assume Maximum Production speed is natively granted by the hive.
// Base Output (PER BEE) sould be 256, so 1 bee is 256 combs of X Type, 4 would be 1024 Combs, etc.
// Feeding Nutrient Fluid to bees at the start of a recipe will uh... Do stuff..
// Some Nutrients Reduce Time
// Some Nutrients Boost Productivity (Flat Multiplier to all outputs)
// Each Bee Installed in a bee holder will cost 1 ZPM amp - meaning atm the Best In Slot HONEY will draw 64A ZPM
// until V8+

// Check out ResearchStationMachine as to how to make the BeeHolderPartMachine lock it's I/O in our custom logic
// I tried to Impl some of it, probably not sufficient.
