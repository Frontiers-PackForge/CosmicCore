package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.common.machine.multiblock.part.BeeHolderPartMachine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import lombok.Getter;

public class MegaAlvearyMultiblockMachine extends WorkableElectricMultiblockMachine {

    @Getter
    private static BeeHolderPartMachine beeHolder;

    public MegaAlvearyMultiblockMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    public static class MegaAlvearyRecipeLogic extends RecipeLogic {

        public MegaAlvearyRecipeLogic(MegaAlvearyMultiblockMachine machine) {
            super(machine);
        }

        @Override
        public MetaMachine getMachine() {
            return (MegaAlvearyMultiblockMachine) super.getMachine();
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

        @Override
        protected ActionResult handleRecipeIO(GTRecipe recipe, IO io) {
            // If the Machine has became active, lock BeeHolders.
            // Otherwise just unlock them.
            // We never want to 'delete' the Queen So i didn't copy much more about the object Holder lest i screw up.
            // However because we are overriding handleIO it means we also need to properly rehandle the outputs i'd
            // assume.

            if (machine.isActive() || machine.isSuspendAfterFinish()) {
                beeHolder.setLocked(true);
                return ActionResult.SUCCESS;
            }

            beeHolder.setLocked(false);
            return ActionResult.SUCCESS;
        }
    }
}
