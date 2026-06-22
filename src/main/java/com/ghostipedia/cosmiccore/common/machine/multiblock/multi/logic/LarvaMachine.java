package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;
import com.ghostipedia.cosmiccore.utils.ItemData;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.core.component.DataComponents;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.fluids.FluidStack;

import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.SCANNER_RECIPES;
import static com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour.getCircuitConfiguration;

public class LarvaMachine extends WorkableElectricMultiblockMachine {

    private static ItemStack getNamedPaper(String name) {
        ItemStack stack = new ItemStack(Items.PAPER);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return stack;
    }

    public LarvaMachine(BlockEntityCreationInfo holder) {
        super(holder, m -> new LarvaRecipeLogic((LarvaMachine) m));
    }

    public static String ASTROID_NBT_TYPE = "AsteroidType";
    public static String ASTROID_NBT_TIER = "Tier";
    public static String ASTEROID_SIZE = "Size";

    private static Map<ItemStack, Pair<Integer, ItemStack>> LARVA_LOOTTABLE = null;
    private static Map<ItemStack, Integer> LARVA_TIERS = null;
    private static Map<Integer, Pair<ItemStack, FluidStack>> LARVA_INPUTS = null;
    private static Map<ItemStack, ItemStack> RESEARCH_RECIPES = null;

    public static int getAsteroidSize(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        return ItemData.readTag(stack).getInt(ASTEROID_SIZE);
    }

    private static ItemStack setAsteroidSize(ItemStack stack, int size) {
        if (stack.isEmpty()) return stack;
        ItemData.mutateTag(stack, tag -> tag.putInt(ASTEROID_SIZE, size));
        return stack;
    }

    public static ItemStack getAstroidDataChip(String id, int tier) {
        ItemStack stack = CosmicItems.TARGETING_CHIP.asStack();
        ItemData.mutateTag(stack, tag -> {
            tag.putString(ASTROID_NBT_TYPE, id);
            tag.putInt(ASTROID_NBT_TIER, tier);
        });
        return stack;
    }

    // This gets called from our CosmicRecipes class, not anywhere here. Put here to centralize recipe creation.
    public static void generateTargettingChipRecipes(RecipeOutput provider) {
        SCANNER_RECIPES.recipeBuilder(CosmicCore.id("carbonic_asteroid"))
                .inputItems(new ItemStack(Blocks.IRON_ORE.asItem(), 1))
                .inputItems(CosmicItems.TARGETING_CHIP.asStack())
                .outputItems(getAstroidDataChip("carbonic_asteroid", 1))
                .duration(40)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);
        SCANNER_RECIPES.recipeBuilder(CosmicCore.id("ferric_asteroid"))
                .inputItems(new ItemStack(Blocks.IRON_ORE.asItem(), 1))
                .inputItems(CosmicItems.TARGETING_CHIP.asStack())
                .outputItems(getAstroidDataChip("ferric_asteroid", 1))
                .duration(40)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);

        SCANNER_RECIPES.recipeBuilder(CosmicCore.id("rare_metal_asteroid"))
                .inputItems(TagPrefix.rawOre, GTMaterials.Cooperite)
                .inputItems(CosmicItems.TARGETING_CHIP.asStack())
                .outputItems(getAstroidDataChip("rare_metal_asteroid", 1))
                .duration(40)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);
        SCANNER_RECIPES.recipeBuilder(CosmicCore.id("auric_asteroid"))
                .inputItems(TagPrefix.rawOre, GTMaterials.Gold)
                .inputItems(CosmicItems.TARGETING_CHIP.asStack())
                .outputItems(getAstroidDataChip("auric_asteroid", 1))
                .duration(40)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);
        SCANNER_RECIPES.recipeBuilder(CosmicCore.id("brimstone_asteroid"))
                .inputItems(TagPrefix.dust, GTMaterials.Netherrack)
                .inputItems(CosmicItems.TARGETING_CHIP.asStack())
                .outputItems(getAstroidDataChip("brimstone_asteroid", 1))
                .duration(40)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);
        SCANNER_RECIPES.recipeBuilder(CosmicCore.id("lith_asteroid"))
                .inputItems(TagPrefix.rawOre, GTMaterials.CertusQuartz)
                .inputItems(CosmicItems.TARGETING_CHIP.asStack())
                .outputItems(getAstroidDataChip("lith_asteroid", 1))
                .duration(40)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);
        SCANNER_RECIPES.recipeBuilder(CosmicCore.id("mafic_asteroid"))
                .inputItems(TagPrefix.rawOre, GTMaterials.Tungstate)
                .inputItems(CosmicItems.TARGETING_CHIP.asStack())
                .outputItems(getAstroidDataChip("mafic_asteroid", 1))
                .duration(40)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);
        SCANNER_RECIPES.recipeBuilder(CosmicCore.id("mossy_asteroid"))
                .inputItems(TagPrefix.rawOre, GTMaterials.Emerald)
                .inputItems(CosmicItems.TARGETING_CHIP.asStack())
                .outputItems(getAstroidDataChip("mossy_asteroid", 1))
                .duration(40)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);
        SCANNER_RECIPES.recipeBuilder(CosmicCore.id("occult_asteroid"))
                .inputItems(TagPrefix.rawOre, GTMaterials.Alunite)
                .inputItems(CosmicItems.TARGETING_CHIP.asStack())
                .outputItems(getAstroidDataChip("occult_asteroid", 1))
                .duration(40)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);
        SCANNER_RECIPES.recipeBuilder(CosmicCore.id("oxide_asteroid"))
                .inputFluids(GTMaterials.Ice, 1000)
                .inputItems(CosmicItems.TARGETING_CHIP.asStack())
                .outputItems(getAstroidDataChip("oxide_asteroid", 1))
                .duration(40)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);
        SCANNER_RECIPES.recipeBuilder(CosmicCore.id("sanguine_asteroid"))
                .inputItems(TagPrefix.rawOre, CosmicMaterials.Moondrop)
                .inputItems(CosmicItems.TARGETING_CHIP.asStack())
                .outputItems(getAstroidDataChip("sanguine_asteroid", 1))
                .duration(40)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);
        SCANNER_RECIPES.recipeBuilder(CosmicCore.id("wasteland_asteroid"))
                .inputItems(TagPrefix.rawOre, CosmicMaterials.PaleOreBad)
                .inputItems(CosmicItems.TARGETING_CHIP.asStack())
                .outputItems(getAstroidDataChip("wasteland_asteroid", 1))
                .duration(40)
                .EUt(GTValues.VA[GTValues.HV])
                .save(provider);
    }

    private static Map<ItemStack, Pair<Integer, ItemStack>> getLarvaLoottable() {
        if (LARVA_LOOTTABLE == null) {
            LARVA_LOOTTABLE = new HashMap<>();
            // spotless: off
            // Beetle Data Orb (NC) -> Tier (0-based)-> -> Astroid
            LARVA_LOOTTABLE.put(getAstroidDataChip("carbonic_asteroid", 1),
                    Pair.of(0, CosmicItems.CARBON_ASTEROID.asStack()));
            LARVA_LOOTTABLE.put(getAstroidDataChip("ferric_asteroid", 1),
                    Pair.of(0, CosmicItems.FERRIC_ASTEROID.asStack()));
            LARVA_LOOTTABLE.put(getAstroidDataChip("rare_metal_asteroid", 1),
                    Pair.of(0, CosmicItems.RARE_METAL_ASTEROID.asStack()));
            LARVA_LOOTTABLE.put(getAstroidDataChip("auric_asteroid", 1),
                    Pair.of(0, CosmicItems.AURIC_ASTEROID.asStack()));
            LARVA_LOOTTABLE.put(getAstroidDataChip("brimstone_asteroid", 1),
                    Pair.of(0, CosmicItems.BRIMSTONE_ASTEROID.asStack()));
            LARVA_LOOTTABLE.put(getAstroidDataChip("lith_asteroid", 1),
                    Pair.of(0, CosmicItems.LITH_ASTEROID.asStack()));
            LARVA_LOOTTABLE.put(getAstroidDataChip("mafic_asteroid", 1),
                    Pair.of(0, CosmicItems.MAFIC_ASTEROID.asStack()));
            LARVA_LOOTTABLE.put(getAstroidDataChip("mossy_asteroid", 1),
                    Pair.of(0, CosmicItems.MOSSY_ASTEROID.asStack()));
            LARVA_LOOTTABLE.put(getAstroidDataChip("occult_asteroid", 1),
                    Pair.of(0, CosmicItems.OCCULT_ASTEROID.asStack()));
            LARVA_LOOTTABLE.put(getAstroidDataChip("oxide_asteroid", 1),
                    Pair.of(0, CosmicItems.OXIDE_ASTEROID.asStack()));
            LARVA_LOOTTABLE.put(getAstroidDataChip("sanguine_asteroid", 1),
                    Pair.of(0, CosmicItems.SANGUINE_ASTEROID.asStack()));
            LARVA_LOOTTABLE.put(getAstroidDataChip("wasteland_asteroid", 1),
                    Pair.of(0, CosmicItems.WASTELAND_ASTEROID.asStack()));
            // spotless: on
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
            LARVA_INPUTS.put(0, Pair.of(CosmicItems.TUNGSTENSTEEL_NANOLATTICE_SPOOL.asStack(4),
                    GTMaterials.RocketFuel.getFluid(8000)));
            LARVA_INPUTS.put(1, Pair.of(CosmicItems.TUNGSTENSTEEL_NANOLATTICE_SPOOL.asStack(16),
                    GTMaterials.RocketFuel.getFluid(16000)));
            LARVA_INPUTS.put(2, Pair.of(CosmicItems.TUNGSTENSTEEL_NANOLATTICE_SPOOL.asStack(64),
                    GTMaterials.RocketFuel.getFluid(64000)));
            LARVA_INPUTS.put(3, Pair.of(CosmicItems.TUNGSTENSTEEL_NANOLATTICE_SPOOL.asStack(256),
                    GTMaterials.RocketFuel.getFluid(256000)));
            LARVA_INPUTS.put(4, Pair.of(CosmicItems.TUNGSTENSTEEL_NANOLATTICE_SPOOL.asStack(1024),
                    GTMaterials.RocketFuel.getFluid(1024000)));
        }
        return LARVA_INPUTS;
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
                // Should do it? This'll set the NBT of the asteroid appropriately
                var sizedAsteroid = setAsteroidSize(itemOutput, getCircuitConfiguration(circuitStack));

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
                    finalRecipeItemOutputs.add(sizedAsteroid);
                } else {
                    // not enough inputs
                }

            }
            if (finalRecipeItemOutputs.isEmpty()) {
                return Collections.emptyIterator();
            }

            var builder = GTRecipeBuilder
                    .of(CosmicCore.id("larva_recipe"), CosmicRecipeTypes.BEES)
                    .EUt(GTValues.VA[GTValues.ZPM])
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

            return Collections.singleton(builder.build()).iterator();
        }

        // Helper function for accessing map based on Itemstack.isSameItemSameTags

        private static <V> V mapGet(Map<ItemStack, V> map, ItemStack item) {
            if (item == null) return null;
            for (var entry : map.entrySet()) {
                if (ItemStack.isSameItemSameComponents(item, entry.getKey())) {
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
                if (ItemStack.isSameItemSameComponents(stack, toConsume)) {
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
                if (ItemStack.isSameItemSameComponents(stack, toConsume)) {
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
