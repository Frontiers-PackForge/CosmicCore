package com.ghostipedia.cosmiccore.common.data.recipe;

import com.ghostipedia.cosmiccore.common.machine.multiblock.electric.MagneticFieldMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.part.SterilizationHatchPartMachine;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

import static com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys.PLASMA;

public class CosmicRecipeModifiers {

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
                        .durationMultiplier(actualParallel / 2F * 0.25F)
                        .build();
            }
        }
        return ModifierFunction.IDENTITY;
    }

    public static @NotNull ModifierFunction groveMulti(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof IRecipeLogicMachine rlm)) return ModifierFunction.NULL;
        final Item match = BuiltInRegistries.ITEM.get(new ResourceLocation("ars_nouveau:drygmy_charm"));
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
        return ModifierFunction.builder()
                .outputModifier(ContentModifier.multiplier(multiplier))
                .build();
    }

    public static @NotNull ModifierFunction sterileHatch(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (machine instanceof IMultiController controller && controller.isFormed()) {
            var parts = controller.getParts();
            var sterileHatch = parts.stream()
                    .filter(part -> part instanceof SterilizationHatchPartMachine)
                    .findAny();
            if (sterileHatch.isPresent()) {
                var copy = recipe.copy();
                var inputs = copy.tickInputs.getOrDefault(FluidRecipeCapability.CAP, new ArrayList<>());
                var fluidStack = GTMaterials.Chlorine.getFluid(PLASMA, 15);
                inputs.add(new Content(FluidIngredient.of(
                        TagUtil.createFluidTag(BuiltInRegistries.FLUID.getKey(fluidStack.getFluid()).getPath()),
                        fluidStack.getAmount(), fluidStack.getTag()), 10000, 10000, 0));
                copy.tickInputs.put(FluidRecipeCapability.CAP, inputs);
                return (c) -> copy;
            }
            return ModifierFunction.IDENTITY;
        }
        return ModifierFunction.NULL;
    }
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
