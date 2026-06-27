package com.ghostipedia.cosmiccore.gtbridge.recipemaker;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Mutable in-progress recipe being authored by the recipe-maker tool. Slot counts are not fixed: they are
 * derived from the selected {@link GTRecipeType}'s capability limits ({@code maxInputs}/{@code maxOutputs}),
 * which is what lets one UI cover every GregTech recipe type instead of a hand-written layout per type.
 * Item-output chances are parallel to {@link #itemOutputs}; {@link #GUARANTEED} means an unconditional output.
 */
public class RecipeDraft {

    public static final int GUARANTEED = 10000;

    public GTRecipeType recipeType;

    public final List<ItemStack> itemInputs = new ArrayList<>();
    public final List<String> itemInputTags = new ArrayList<>();
    public final List<Boolean> itemInputNotConsumed = new ArrayList<>();
    public final List<Integer> itemInputChances = new ArrayList<>();
    public final List<Integer> itemInputBoosts = new ArrayList<>();
    public final List<ItemStack> itemOutputs = new ArrayList<>();
    public final List<Integer> itemOutputChances = new ArrayList<>();
    public final List<Integer> itemOutputBoosts = new ArrayList<>();
    public final List<FluidStack> fluidInputs = new ArrayList<>();
    public final List<Integer> fluidInputChances = new ArrayList<>();
    public final List<Integer> fluidInputBoosts = new ArrayList<>();
    public final List<FluidStack> fluidOutputs = new ArrayList<>();
    public final List<Integer> fluidOutputChances = new ArrayList<>();
    public final List<Integer> fluidOutputBoosts = new ArrayList<>();
    public final List<String> extraLines = new ArrayList<>();

    public int voltageTier = GTValues.LV;
    public long amperage = 1L;
    public int duration = 100;

    public boolean rawEU = false;
    public long rawVoltage = 32L;
    public String voltageArray = "VA";

    public int blastTemp = 0;
    public int cwu = 0;
    public String cleanroom = "none";
    public String dimension = "";

    public int maxItemInputs() {
        return recipeType == null ? 0 : recipeType.getMaxInputs(ItemRecipeCapability.CAP);
    }

    public int maxItemOutputs() {
        return recipeType == null ? 0 : recipeType.getMaxOutputs(ItemRecipeCapability.CAP);
    }

    public int maxFluidInputs() {
        return recipeType == null ? 0 : recipeType.getMaxInputs(FluidRecipeCapability.CAP);
    }

    public int maxFluidOutputs() {
        return recipeType == null ? 0 : recipeType.getMaxOutputs(FluidRecipeCapability.CAP);
    }

    public void setRecipeType(GTRecipeType type) {
        this.recipeType = type;
        itemInputs.clear();
        itemOutputs.clear();
        itemOutputChances.clear();
        fluidInputs.clear();
        fluidOutputs.clear();
    }

    public void setItemOutput(int index, ItemStack stack, int chance) {
        while (itemOutputs.size() <= index) {
            itemOutputs.add(ItemStack.EMPTY);
            itemOutputChances.add(GUARANTEED);
        }
        itemOutputs.set(index, stack);
        itemOutputChances.set(index, chance);
    }

    public int chanceOf(int index) {
        return index >= 0 && index < itemOutputChances.size() ? itemOutputChances.get(index) : GUARANTEED;
    }
}
