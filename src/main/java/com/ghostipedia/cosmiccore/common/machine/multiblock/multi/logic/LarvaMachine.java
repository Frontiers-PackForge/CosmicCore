package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fluids.FluidStack;

import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials.DilutedPrisma;
import static com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials.Prisma;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Water;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.SCANNER_RECIPES;
import static com.gregtechceu.gtceu.common.item.IntCircuitBehaviour.getCircuitConfiguration;

public class LarvaMachine extends WorkableElectricMultiblockMachine {

    private static ItemStack getNamedPaper(String name) {
        ItemStack stack = new ItemStack(Items.PAPER);
        stack.setHoverName(Component.literal(name));
        return stack;
    }

    public LarvaMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    public static String ASTROID_NBT = "cosmic_core_astroid";

    private static Map<ItemStack, Pair<Integer, ItemStack>> LARVA_LOOTTABLE = null;
    private static Map<ItemStack, Integer> LARVA_TIERS = null;
    private static Map<Integer, Pair<ItemStack, FluidStack>> LARVA_INPUTS = null;
    private static Map<ItemStack, ItemStack> RESEARCH_RECIPES = null;

    private static ItemStack getAstroidDataChip(String id){
        ItemStack stack = CosmicItems.TARGETING_CHIP.asStack();
        stack.getOrCreateTag().putString(ASTROID_NBT, id);
        return stack;
    }

    // This gets called from our CosmicRecipes class, not anywhere here. Put here to centralize recipe creation.
    public static void generateTargettingChipRecipes(Consumer<FinishedRecipe> provider) {
        SCANNER_RECIPES.recipeBuilder(CosmicCore.id("iron_astroid"))
                .inputItems(new ItemStack(Blocks.IRON_ORE.asItem(), 1))
                .outputItems(getAstroidDataChip("iron_astroid"))
                .duration(40)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);
    }


    private static Map<ItemStack, Pair<Integer, ItemStack>> getLarvaLoottable() {
        if (LARVA_LOOTTABLE == null) {
            LARVA_LOOTTABLE = new HashMap<>();
            // Beetle Data Orb (NC) -> Tier (0-based)-> -> Astroid
            LARVA_LOOTTABLE.put(getAstroidDataChip("iron_astroid"), Pair.of(0, CosmicItems.CARBON_ASTEROID.asStack()));
            LARVA_LOOTTABLE.put(CosmicItems.TARGETING_CHIP.asStack(), Pair.of(0, CosmicItems.FERRIC_ASTEROID.asStack()));
            LARVA_LOOTTABLE.put(CosmicItems.TARGETING_CHIP.asStack(), Pair.of(0, CosmicItems.RARE_METAL_ASTEROID.asStack()));
            LARVA_LOOTTABLE.put(CosmicItems.TARGETING_CHIP.asStack(), Pair.of(0, CosmicItems.AURIC_ASTEROID.asStack()));
            LARVA_LOOTTABLE.put(CosmicItems.TARGETING_CHIP.asStack(), Pair.of(0, CosmicItems.BRIMSTONE_ASTEROID.asStack()));
            LARVA_LOOTTABLE.put(CosmicItems.TARGETING_CHIP.asStack(), Pair.of(0, CosmicItems.LITH_ASTEROID.asStack()));
            LARVA_LOOTTABLE.put(CosmicItems.TARGETING_CHIP.asStack(), Pair.of(0, CosmicItems.MAFIC_ASTEROID.asStack()));
            LARVA_LOOTTABLE.put(CosmicItems.TARGETING_CHIP.asStack(), Pair.of(0, CosmicItems.MOSSY_ASTEROID.asStack()));
            LARVA_LOOTTABLE.put(CosmicItems.TARGETING_CHIP.asStack(), Pair.of(0, CosmicItems.OCCULT_ASTEROID.asStack()));
            LARVA_LOOTTABLE.put(CosmicItems.TARGETING_CHIP.asStack(), Pair.of(0, CosmicItems.OXIDE_ASTEROID.asStack()));
            LARVA_LOOTTABLE.put(CosmicItems.TARGETING_CHIP.asStack(), Pair.of(0, CosmicItems.SANGUINE_ASTEROID.asStack()));
            LARVA_LOOTTABLE.put(CosmicItems.TARGETING_CHIP.asStack(), Pair.of(0, CosmicItems.WASTELAND_ASTEROID.asStack()));

        }
        return LARVA_LOOTTABLE;
    }

    private static Map<ItemStack, Integer> getLarvaTiers() {
        if (LARVA_TIERS == null) {
            LARVA_TIERS = new HashMap<>();
            LARVA_TIERS.put(CosmicItems.HAULER_PROBE_GRADE_1.asStack(), 0);
            LARVA_TIERS.put(CosmicItems.HAULER_PROBE_GRADE_2.asStack(), 1);
            LARVA_TIERS.put(CosmicItems.HAULER_PROBE_GRADE_3.asStack(), 2);
            LARVA_TIERS.put(CosmicItems.HAULER_PROBE_GRADE_4.asStack(), 3);
            LARVA_TIERS.put(CosmicItems.HAULER_PROBE_GRADE_5.asStack(), 4);
        }
        return LARVA_TIERS;
    }

    private static Map<Integer, Pair<ItemStack, FluidStack>> getLarvaInputs() {
        if (LARVA_INPUTS == null) {
            LARVA_INPUTS = new HashMap<>();
            LARVA_INPUTS.put(0, Pair.of(CosmicItems.TUNGSTENSTEEL_NANOLATTICE_SPOOL.asStack(16), GTMaterials.RocketFuel.getFluid(8000)));
            LARVA_INPUTS.put(1, Pair.of(CosmicItems.TUNGSTENSTEEL_NANOLATTICE_SPOOL.asStack(64), GTMaterials.RocketFuel.getFluid(16000)));
            LARVA_INPUTS.put(2, Pair.of(CosmicItems.TUNGSTENSTEEL_NANOLATTICE_SPOOL.asStack(256), GTMaterials.RocketFuel.getFluid(64000)));
            LARVA_INPUTS.put(3, Pair.of(CosmicItems.TUNGSTENSTEEL_NANOLATTICE_SPOOL.asStack(1024), GTMaterials.RocketFuel.getFluid(256000)));
            LARVA_INPUTS.put(4, Pair.of(CosmicItems.TUNGSTENSTEEL_NANOLATTICE_SPOOL.asStack(4096), GTMaterials.RocketFuel.getFluid(1024000)));
        }
        return LARVA_INPUTS;
    }

    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object @NotNull... args) {
        return new LarvaRecipeLogic(this);
    }

    public static class LarvaRecipeLogic extends RecipeLogic {

        public LarvaRecipeLogic(LarvaMachine machine) {
            super(machine);
        }

        @Override
        public @NotNull Iterator<GTRecipe> searchRecipe() {
            var larvaMachine = (LarvaMachine) machine;

            // Available Fluid Stacks in the multiblock (for checking cable+coolant)
            var availableFluids = new ArrayList<FluidStack>();
            // Available Item Stacks in the multiblock (for checking cable+coolant)
            var availableItems = new ArrayList<ItemStack>();

            // Inputs set to be consumed
            var finalRecipeItemInputs = new ArrayList<ItemStack>();
            var finalRecipeFluidInputs = new ArrayList<FluidStack>();

            // Outputs for the recipe
            var finalRecipeItemOutputs = new ArrayList<ItemStack>();

            var fluidHandlers = machine.getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP);
            for (var handler : fluidHandlers) {
                if (!(handler instanceof NotifiableFluidTank itemHandler)) continue;
                for (var content : itemHandler.getContents()) {
                    if (!(content instanceof FluidStack stack)) continue;
                    availableFluids.add(stack.copy());
                }
            }

            var itemHandlers = machine.getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP);
            for (var handler : itemHandlers) {
                if (!(handler instanceof NotifiableItemStackHandler itemHandler)) continue;
                for (var content : itemHandler.getContents()) {
                    if (!(content instanceof ItemStack stack)) continue;
                    availableItems.add(stack.copy());
                }
            }

            var tiers = getLarvaTiers();
            var lootTable = getLarvaLoottable();
            var inputs = getLarvaInputs();

            var parts = larvaMachine.getParts();
            for (var part : parts) {
                // TODO: Make this specifically be our new bus
                if (!(part instanceof ItemBusPartMachine itemBus)) continue;
                if (!itemBus.getInventory().handlerIO.equals(IO.IN)) continue;

                // Find tier and output
                // Enforced through the filters in the bus: 1 larva and 1 data orb per bus
                var tier = -1;
                ItemStack output = null;

                for (var content : itemBus.getInventory().getContents()) {
                    if (!(content instanceof ItemStack stack)) continue;
                    var tempTier = mapGet(tiers, stack);
                    if (tempTier == null) continue;
                    tier = tempTier;
                }
                if (tier == -1) continue;

                for (var content : itemBus.getInventory().getContents()) {
                    if (!(content instanceof ItemStack stack)) continue;
                    var loot = mapGet(lootTable, stack);
                    if (loot == null) continue;
                    // Tier is lower than needed for recipe; Throw here? Let the user know somehow?
                    if (tier < loot.getFirst()) continue;
                    output = loot.getSecond();
                }
                if (output == null) continue;

                // TODO: For now circuit is a dumb parallel with 0 being 1x, 1 being 2x etc

                var circuit = itemBus.getCircuitInventory();
                var circuitStack = circuit.getStackInSlot(0);
                var multiplier = getCircuitConfiguration(circuitStack) + 1;

                var recipeInputs = inputs.get(tier);
                var itemInput = recipeInputs.getFirst().copy();
                var fluidInput = recipeInputs.getSecond().copy();
                var itemOutput = output.copy();
                itemInput.setCount(recipeInputs.getFirst().getCount() * multiplier);
                fluidInput.setAmount(recipeInputs.getSecond().getAmount() * multiplier);
                // TODO: change this to use NBT / modify output in different way
                itemOutput.setCount(itemOutput.getCount() * multiplier);

                if (canConsumeItem(availableItems, itemInput) &&
                        canConsumeFluid(availableFluids, fluidInput)) {
                    // Subtract the inputs from our list of available inputs
                    consumeItem(availableItems, itemInput);
                    // Subtract the inputs from our list of available inputs
                    consumeItem(availableItems, itemInput);
                    consumeFluid(availableFluids, fluidInput);

                    // actually add inputs and outputs to the lists for the final recipe
                    finalRecipeItemInputs.add(itemInput);
                    finalRecipeFluidInputs.add(fluidInput);
                    finalRecipeItemOutputs.add(itemOutput);
                } else {
                    // not enough inputs
                }

            }
            if (finalRecipeItemOutputs.isEmpty()) {
                return Collections.emptyIterator();
            }

            var builder = GTRecipeBuilder
                    .of(CosmicCore.id("larva_recipe"), CosmicRecipeTypes.BEES)
                    .EUt(GTValues.VA[GTValues.LV])
                    .duration(20 * 60);

            for (var itemInput : finalRecipeItemInputs) {
                builder.inputItems(itemInput);
            }
            for (var inputFluid : finalRecipeFluidInputs) {
                builder.inputFluids(inputFluid);
            }
            for (var outputItem : finalRecipeItemOutputs) {
                builder.outputItems(outputItem);
            }

            return Collections.singleton(builder.buildRawRecipe()).iterator();
        }

        // Helper function for accessing map based on Itemstack.isSameItemSameTags

        private static <V> V mapGet(Map<ItemStack, V> map, ItemStack item) {
            if (item == null) return null;
            for (var entry : map.entrySet()) {
                if (ItemStack.isSameItemSameTags(item, entry.getKey())) {
                    return entry.getValue();
                }
            }
            return null;
        }

        /**
         * Checks if the given item can be fully consumed from the list.
         */
        private static boolean canConsumeItem(List<ItemStack> available, ItemStack toConsume) {
            if (toConsume.isEmpty()) {
                return true;
            }

            int remaining = toConsume.getCount();

            for (ItemStack stack : available) {
                if (ItemStack.isSameItemSameTags(stack, toConsume)) {
                    remaining -= stack.getCount();
                    if (remaining <= 0) {
                        return true;
                    }
                }
            }

            return false;
        }

        /**
         * Attempts to consume the given item from the list.
         * Returns true if the full amount was successfully removed, false otherwise.
         * Mutates the list’s stack counts and empties stacks as needed.
         */
        private static boolean consumeItem(List<ItemStack> available, ItemStack toConsume) {
            if (toConsume.isEmpty()) {
                return true;
            }

            int remaining = toConsume.getCount();

            for (ItemStack stack : available) {
                if (ItemStack.isSameItemSameTags(stack, toConsume)) {
                    int taken = Math.min(stack.getCount(), remaining);
                    stack.shrink(taken);
                    remaining -= taken;
                    if (remaining <= 0) {
                        return true;
                    }
                }
            }

            return false; // not enough found
        }

        /**
         * Checks if the given fluid can be fully consumed from the list.
         */
        private static boolean canConsumeFluid(List<FluidStack> available, FluidStack toConsume) {
            if (toConsume.isEmpty()) {
                return true;
            }

            int needed = toConsume.getAmount();

            for (FluidStack stack : available) {
                if (toConsume.isFluidEqual(stack)) {
                    needed -= stack.getAmount();
                    if (needed <= 0) {
                        return true;
                    }
                }
            }

            return false;
        }

        /**
         * Removes the given fluid amount from matching stacks in the list.
         * Returns true if the full amount was successfully removed, false otherwise.
         * Mutates the list’s FluidStacks.
         */
        private static boolean consumeFluid(List<FluidStack> available, FluidStack toConsume) {
            if (toConsume.isEmpty()) {
                return true;
            }

            int remaining = toConsume.getAmount();

            for (FluidStack stack : available) {
                if (toConsume.isFluidEqual(stack)) {
                    int taken = Math.min(stack.getAmount(), remaining);
                    stack.shrink(taken);
                    remaining -= taken;

                    if (remaining <= 0) {
                        return true;
                    }
                }
            }

            return false;
        }
    }
}
