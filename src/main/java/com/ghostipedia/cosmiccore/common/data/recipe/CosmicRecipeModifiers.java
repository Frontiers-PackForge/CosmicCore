package com.ghostipedia.cosmiccore.common.data.recipe;

import com.ghostipedia.cosmiccore.api.machine.multiblock.StellarBaseModule;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.machine.multiblock.electric.MagneticFieldMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.LarvaMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.TitanFusionReactorMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.part.ModuleHatchPartMachine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

public class CosmicRecipeModifiers {

    public static final RecipeModifier COSMIC_MODULES = CosmicRecipeModifiers::moduleParallel;

    /**
     * Recipe modifier for Stellar Modules.
     * Uses the module's configured voltage and parallel settings.
     * - Applies overclocking based on configured voltage per parallel
     * - Applies parallelization up to the effective parallel limit (min of configured and Iris limit)
     */
    public static final RecipeModifier STELLAR_MODULE_OVERCLOCK = CosmicRecipeModifiers::stellarModuleOverclock;

    /**
     * Stellar module overclock logic.
     * Uses configuredVoltagePerParallel for OC tier and configuredMaxParallel for parallels.
     */
    public static @NotNull ModifierFunction stellarModuleOverclock(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof StellarBaseModule module)) {
            return ModifierFunction.NULL;
        }

        // Check if recipe tier is within our configured tier
        int recipeTier = RecipeHelper.getRecipeEUtTier(recipe);
        int moduleTier = module.getOverclockTier();
        if (recipeTier > moduleTier) {
            return ModifierFunction.NULL; // Recipe requires higher tier than configured
        }

        // Get the effective parallel limit (min of user config and Iris limit)
        int maxParallels = module.getEffectiveParallelLimit();

        // Calculate actual parallels based on available resources
        int actualParallels = ParallelLogic.getParallelAmount(machine, recipe, maxParallels);
        if (actualParallels == 0) {
            return ModifierFunction.NULL;
        }

        // Calculate maximum voltage for overclocking
        // Total voltage = voltage per parallel * number of parallels
        long maxVoltage = module.getConfiguredVoltagePerParallel() * actualParallels;

        // Apply overclock using non-perfect subtick logic
        // This uses the configured voltage as the maximum
        var ocModifier = OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK.getModifier(
                machine, recipe, maxVoltage, false); // Don't let OC logic add more parallels

        // Apply parallel modifier
        if (actualParallels > 1) {
            var parallelModifier = ModifierFunction.builder()
                    .modifyAllContents(ContentModifier.multiplier(actualParallels))
                    .eutMultiplier(actualParallels)
                    .parallels(actualParallels)
                    .build();
            return ocModifier.andThen(parallelModifier);
        }

        return ocModifier;
    }

    public static ModifierFunction asteroidYieldModifier(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof IRecipeLogicMachine recipeLogicMachine)) {
            return ModifierFunction.NULL;
        }
        int size = 1;
        var handlers = recipeLogicMachine.getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP);
        for (var handler : handlers) {
            for (var content : handler.getContents()) {
                if (content instanceof ItemStack stack && !stack.isEmpty()) {
                    size = Math.max(size, LarvaMachine.getAsteroidSize(stack));

                }
            }
        }
        if (size == 1) return ModifierFunction.IDENTITY;
        int cap = ParallelLogic.limitByOutputMerging(recipeLogicMachine, recipe, size,
                recipeLogicMachine::canVoidRecipeOutputs, Collections.emptyList());
        if (cap <= 1) return ModifierFunction.IDENTITY;

        return ModifierFunction.builder()
                .outputModifier(ContentModifier.multiplier(cap))
                .build();
    }

    public static ModifierFunction vomahineReactorOC(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof MagneticFieldMachine magnetMachine)) {
            return RecipeModifier.nullWrongType(MagneticFieldMachine.class, machine);
        }
        final var magnetStrength = magnetMachine.getFieldStrength();
        long EUt = recipe.getOutputEUt().getTotalEU();
        int actualParallel = ParallelLogic.getParallelAmount(magnetMachine, recipe, 16);
        long maxReactorVoltage = magnetMachine.getOverclockVoltage();
        float recipeDuration = (recipe.duration);
        float durationModifier = recipeDuration * actualParallel / 20;
        // Parallel is ALWAYS capped to 16
        // Check that the damn thing actually creates EU
        if (EUt <= 0 || maxReactorVoltage <= EUt) return ModifierFunction.NULL;
        if (!recipe.data.contains("min_field") || recipe.data.getInt("min_field") > magnetStrength) {
            return ModifierFunction.NULL;
        }
        if (!magnetMachine.isGenerator()) {
            if (RecipeHelper.getRecipeEUtTier(recipe) > magnetMachine.getTier()) {
                return ModifierFunction.NULL;
            }
        }
        // EU Outputs is always 16A of the respective recipe (If it can).
        return ModifierFunction.builder()
                .inputModifier(ContentModifier.multiplier(actualParallel))
                // .durationMultiplier(durationModifier) this just actually causes hell on earth so ignore for now
                .eutMultiplier(actualParallel)
                .parallels(actualParallel)
                .build();
    }

    public static ModifierFunction chemicalVatLogic(MetaMachine machine, GTRecipe recipe) {
        if (machine instanceof WorkableMultiblockMachine vatMachine) {
            if (vatMachine.getParallelHatch().isPresent()) {
                int actualParallel = ParallelLogic.getParallelAmount(vatMachine, recipe,
                        vatMachine.getParallelHatch().get().getCurrentParallel());

                return ModifierFunction.builder()
                        .modifyAllContents(ContentModifier.multiplier(actualParallel))
                        .eutMultiplier(actualParallel * 0.75F)
                        .parallels(actualParallel)
                        .durationMultiplier(actualParallel / 4F)
                        .build();
            }
        }
        return ModifierFunction.IDENTITY;
    }

    public static @NotNull ModifierFunction groveMulti(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof IRecipeLogicMachine rlm)) return ModifierFunction.NULL;
        final Item match = BuiltInRegistries.ITEM.get(ResourceLocation.parse("ars_nouveau:drygmy_charm"));
        var handlers = rlm.getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP);
        int count = 0;
        for (var handler : handlers) {
            for (var content : handler.getContents()) {
                if (content instanceof ItemStack stack && !stack.isEmpty()) {
                    if (stack.is(match)) count += stack.getCount();
                }
            }
        }

        if (count == 1) return ModifierFunction.IDENTITY;
        int multiplier = ParallelLogic.limitByOutputMerging(rlm, recipe, count, rlm::canVoidRecipeOutputs,
                Collections.emptyList());
        if (multiplier == 1) return ModifierFunction.IDENTITY;
        if (multiplier == 0) return ModifierFunction.NULL;
        return ModifierFunction.builder()
                .outputModifier(ContentModifier.multiplier(multiplier))
                .build();
    }

    public static @NotNull ModifierFunction innateParallel4x(MetaMachine machine, GTRecipe recipe) {
        if (machine instanceof WorkableMultiblockMachine parallelMachine) {
            if (parallelMachine.getParallelHatch().isPresent()) {
                int actualParallel = ParallelLogic.getParallelAmount(parallelMachine, recipe,
                        parallelMachine.getParallelHatch().get().getCurrentParallel() * 4);

                return ModifierFunction.builder()
                        .modifyAllContents(ContentModifier.multiplier(actualParallel))
                        .eutMultiplier(actualParallel)
                        .parallels(actualParallel)
                        .build();
            }
        }
        return ModifierFunction.IDENTITY;
    }

    public static @NotNull ModifierFunction titanReactorParallel(MetaMachine machine, GTRecipe recipe) {
        if (machine instanceof TitanFusionReactorMachine parallelMachine) {
            int actualParallel = ParallelLogic.getParallelAmount(parallelMachine, recipe,
                    64 * (parallelMachine.getReactorTier() - 2));
            return ModifierFunction.builder()
                    .modifyAllContents(ContentModifier.multiplier(actualParallel))
                    .eutMultiplier(actualParallel)
                    .parallels(actualParallel)
                    .build();
        }
        return ModifierFunction.IDENTITY;
    }

    private static Map<String, Integer> moduleParallels = null;

    public static Map<String, Integer> getModuleParallels() {
        if (moduleParallels == null) {
            moduleParallels = new Object2IntOpenHashMap<>();
            moduleParallels.put(CosmicItems.PARA_MOD_1.asItem().getDescriptionId(), 16);
            moduleParallels.put(CosmicItems.PARA_MOD_2.asItem().getDescriptionId(), 32);
            moduleParallels.put(CosmicItems.PARA_MOD_3.asItem().getDescriptionId(), 64);
            moduleParallels.put(CosmicItems.PARA_MOD_4.asItem().getDescriptionId(), 256);
        }
        return moduleParallels;
    }

    public static @NotNull ModifierFunction moduleParallel(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof MultiblockControllerMachine multi)) return ModifierFunction.IDENTITY;
        int extraParallels = 0;
        for (var part : multi.getParts()) {
            if (part instanceof ModuleHatchPartMachine modulePart) {
                for (int i = 0; i < modulePart.getInventory().getSlots(); i++) {
                    ItemStack stack = modulePart.getInventory().getStackInSlot(i);
                    extraParallels += getModuleParallels().getOrDefault(stack.getDescriptionId(), 0);
                }
            }
        }
        if (extraParallels == 0) return ModifierFunction.IDENTITY;

        final int finalExtraParallels = extraParallels;
        return (functionRecipe) -> {
            // If we are at only 1 parallel so far,
            // set the max parallels to extraParallels instead of adding to functionRecipe.parallels
            int actualParallel;
            if (functionRecipe.parallels == 1) {
                actualParallel = ParallelLogic.getParallelAmount(machine, recipe, finalExtraParallels);
            } else {
                actualParallel = ParallelLogic.getParallelAmount(machine, recipe,
                        functionRecipe.parallels + finalExtraParallels);

            }

            if (recipe.getType() == GTRecipeTypes.ASSEMBLY_LINE_RECIPES) {
                if (actualParallel > 64) {
                    actualParallel = 64;
                }
            }

            // Set the contents to actualParallel, which means adding actualParallel-1
            var newRecipe = ModifierFunction.builder()
                    .modifyAllContents(ContentModifier.addition(actualParallel - 1))
                    .eutModifier(ContentModifier.addition(actualParallel - 1))
                    .parallels(actualParallel - 1)
                    .build().apply(functionRecipe);
            newRecipe.parallels = actualParallel;
            return newRecipe;
        };
    }
    /*
     * public static @NotNull BiFunction<MetaMachine, GTRecipe, ModifierFunction> sterileHatch(FluidStack stack, boolean
     * perTick) {
     * return (machine, recipe) -> {
     * if (machine instanceof IMultiController controller && controller.isFormed()) {
     * var parts = controller.getParts();
     * var sterileHatch = parts.stream()
     * .filter(part -> part instanceof SterilizationHatchPartMachine)
     * .findAny();
     * if (sterileHatch.isPresent()) {
     * var copy = recipe.copy();
     * 
     * // Change the tickInputs or inputs depending on perTick
     * var inputsMap = perTick ?
     * copy.tickInputs :
     * copy.inputs;
     * var inputs = inputsMap.getOrDefault(FluidRecipeCapability.CAP, new ArrayList<>());
     * 
     * inputs.add(new Content(FluidIngredient.of(
     * TagUtil.createFluidTag(BuiltInRegistries.FLUID.getKey(stack.getFluid()).getPath()),
     * stack.getAmount(), stack.getTag()), 10000, 10000, 0));
     * 
     * if(perTick) {
     * inputsMap.put(FluidRecipeCapability.CAP, inputs);
     * } else {
     * inputsMap.put(FluidRecipeCapability.CAP, inputs);
     * }
     * return (c) -> copy;
     * }
     * return ModifierFunction.IDENTITY;
     * }
     * return ModifierFunction.NULL;
     * };
     * }
     */

    // .recipeModifiers(true,
    // (machine, recipe, OCParams, OCResult) -> {
    // if (machine instanceof IRecipeCapabilityHolder holder) {
    // // Find all the items in the combined Item Input inventories and create oversized ItemStacks
    // Object2IntMap<ItemStack> ingredientStacks =
    // Objects.requireNonNullElseGet(holder.getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP),
    // Collections::<IRecipeHandler<?>>emptyList)
    // .stream()
    // .map(container ->
    // container.getContents().stream().filter(ItemStack.class::isInstance).map(ItemStack.class::cast).toList())
    // .flatMap(container -> GTHashMaps.fromItemStackCollection(container).object2IntEntrySet().stream())
    // .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Integer::sum, () -> new
    // Object2IntOpenCustomHashMap<>(ItemStackHashStrategy.comparingAllButCount())));
    // ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(new
    // ResourceLocation("ars_nouveau:drygmy_charm")));
    // //Never let the multiplier be 0 (THIS IS NOT ACTUALLY PARALLEL, It's just being used to to some goober
    // grade math)
    // if (ingredientStacks.getInt(stack) >= 1) {
    // var maxParallel = ingredientStacks.getInt(stack) / 2;
    // recipe = copyOutputs(recipe, ContentModifier.multiplier(maxParallel));
    // }
    // }
    // return recipe;
    // },
    // GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK))

    // TODO; FIX IT!
    // public static GTRecipe vomahineChemicalPlantParallel(MetaMachine machine, @NotNull GTRecipe recipe, OCParams
    // ocParams, OCResult ocResult) {
    // if (machine instanceof WorkableElectricMultiblockMachine vomahineMachine) {
    // Optional<IParallelHatch> optional = vomahineMachine.getParts().stream().filter(IParallelHatch.class::isInstance)
    // .map(IParallelHatch.class::cast).findAny();
    // if (optional.isPresent()) {
    // IParallelHatch hatch = optional.get();
    // if (hatch.getCurrentParallel() != 0) {
    // var result = GTRecipeModifiers.accurateParallel(machine, recipe, hatch.getCurrentParallel(), false);
    // recipe = result.getFirst() == recipe ? result.getFirst().copy() : result.getFirst();
    // var smartDuration = (recipe.duration * hatch.getCurrentParallel()) / 2;
    // int parallelValue = result.getSecond();
    // recipe.duration = smartDuration;
    // ocResult.init(RecipeHelper.getInputEUt(recipe), smartDuration, parallelValue, ocResult.getOcLevel());
    // return recipe;
    // }
    // }
    // var result = GTRecipeModifiers.accurateParallel(machine, recipe, 0, false);
    // recipe = result.getFirst() == recipe ? result.getFirst().copy() : result.getFirst();
    // var smartDuration = recipe.duration / 2;
    // int parallelValue = result.getSecond();
    // recipe.duration = smartDuration;
    // ocResult.init(RecipeHelper.getInputEUt(recipe), smartDuration, parallelValue, ocResult.getOcLevel());
    // return recipe;
    // }
    // return null;
    // }
}
