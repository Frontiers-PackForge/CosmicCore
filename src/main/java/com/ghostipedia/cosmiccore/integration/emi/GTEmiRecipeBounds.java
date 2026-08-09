package com.ghostipedia.cosmiccore.integration.emi;

import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.gui.CapabilityContentBuilder;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout.CapabilityUIInfo;
import com.gregtechceu.gtceu.api.recipe.gui.RecipeViewerCapabilityLayoutBuilder;
import com.gregtechceu.gtceu.integration.recipeviewer.emi.recipe.GTEmiRecipe;

import dev.emi.emi.api.widget.Bounds;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class GTEmiRecipeBounds {

    private static final int SLOT_SIZE = 18;
    private static final int MIN_WIDTH = 150;
    private static final int ROOT_OVERHEAD = 14;
    private static final int TEXT_LINE_HEIGHT = 9;
    private static final int TEXT_COMPONENT_PADDING = 1;
    private static final int COMPUTATION_LINE_PADDING = 2;
    private static final int OVERCLOCK_BUTTON_HEIGHT = 12;
    private static final String DEFAULT_PROGRESS_SUPPLIER_PREFIX = GTRecipeTypeUILayout.Builder.class.getName() +
            "$$Lambda";
    private static final VarHandle RECIPE_HANDLE = findRecipeHandle();
    private static final VarHandle GRID_BUILDERS_HANDLE = findGridBuildersHandle();
    private static final Map<GTRecipeType, Boolean> DEFAULT_LAYOUTS = new ConcurrentHashMap<>();

    private GTEmiRecipeBounds() {}

    public static void clearCache() {
        DEFAULT_LAYOUTS.clear();
    }

    public static Bounds tryEstimate(GTEmiRecipe emiRecipe) {
        try {
            GTRecipe recipe = getRecipe(emiRecipe);
            if (recipe == null) {
                return null;
            }
            return tryEstimate(recipe);
        } catch (RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    private static Bounds tryEstimate(GTRecipe recipe) {
        GTRecipeType recipeType = recipe.getType();
        GTRecipeTypeUILayout layout = recipeType.getUiLayout();
        if (layout == null || !recipe.conditions.isEmpty() || !layout.getRecipeUIModifiers().isEmpty() ||
                layout.getCustomUIBuilder() != null || !hasDefaultProgressSupplier(layout) ||
                !DEFAULT_LAYOUTS.computeIfAbsent(recipeType, GTEmiRecipeBounds::hasOnlyDefaultCapabilities))
            return null;

        LayoutMetrics metrics = measureLayout(recipe, layout);
        return bounds(metrics.width(), metrics.height());
    }

    private static boolean hasDefaultProgressSupplier(GTRecipeTypeUILayout layout) {
        return layout.getProgressWidgetSupplier().getClass().getName().startsWith(DEFAULT_PROGRESS_SUPPLIER_PREFIX);
    }

    private static boolean hasOnlyDefaultCapabilities(GTRecipeType recipeType) {
        if (GRID_BUILDERS_HANDLE == null) return false;
        for (RecipeCapability<?> capability : recipeType.maxInputs.keySet()) {
            if (!hasDefaultCapability(recipeType, capability)) return false;
        }
        for (RecipeCapability<?> capability : recipeType.maxOutputs.keySet()) {
            if (!hasDefaultCapability(recipeType, capability)) return false;
        }
        return true;
    }

    private static boolean hasDefaultCapability(GTRecipeType recipeType, RecipeCapability<?> capability) {
        GTRecipeTypeUILayout layout = recipeType.getUiLayout();
        CapabilityUIInfo info = layout.capabilityInfo(capability);
        if (capability == ItemRecipeCapability.CAP) {
            return info.recipeViewerLayoutBuilder == RecipeViewerCapabilityLayoutBuilder.ITEM &&
                    info.capabilityWidgetBuilder == CapabilityContentBuilder.ITEM && !hasCustomGrid(info);
        }
        if (capability == FluidRecipeCapability.CAP) {
            return info.recipeViewerLayoutBuilder == RecipeViewerCapabilityLayoutBuilder.FLUID &&
                    info.capabilityWidgetBuilder == CapabilityContentBuilder.FLUID && !hasCustomGrid(info);
        }
        if (capability == EURecipeCapability.CAP) {
            return info.recipeViewerLayoutBuilder == RecipeViewerCapabilityLayoutBuilder.EU &&
                    info.capabilityWidgetBuilder == CapabilityContentBuilder.EU;
        }
        if (capability == CWURecipeCapability.CAP) {
            return info.recipeViewerLayoutBuilder == RecipeViewerCapabilityLayoutBuilder.COMPUTATION &&
                    info.capabilityWidgetBuilder == CapabilityContentBuilder.COMPUTATION;
        }
        return false;
    }

    private static boolean hasCustomGrid(CapabilityUIInfo info) {
        return !((Map<?, ?>) GRID_BUILDERS_HANDLE.get(info)).isEmpty();
    }

    private static LayoutMetrics measureLayout(GTRecipe recipe, GTRecipeTypeUILayout layout) {
        int inputHeight = measureColumnHeight(recipe.getType(), IO.IN);
        int outputHeight = measureColumnHeight(recipe.getType(), IO.OUT);
        int progressSize = Math.max(SLOT_SIZE, layout.getProgressBar().progressSize());
        int contentHeight = Math.max(progressSize, Math.max(inputHeight, outputHeight));
        int height = Math.max(60, ROOT_OVERHEAD + contentHeight + measureTextHeight(recipe));
        return new LayoutMetrics(MIN_WIDTH, height);
    }

    private static int measureColumnHeight(GTRecipeType recipeType, IO io) {
        int height = 0;
        for (RecipeCapability<?> capability : new RecipeCapability<?>[] {
                ItemRecipeCapability.CAP, FluidRecipeCapability.CAP
        }) {
            int capacity = recipeType.getMaxSlots(capability, io);
            if (capacity == 0) continue;
            int columns = Math.min(3, capacity);
            int rows = Math.ceilDiv(capacity, columns);
            height += rows * SLOT_SIZE;
        }
        return height;
    }

    private static int measureTextHeight(GTRecipe recipe) {
        int height = 0;
        int components = 0;
        if (!recipe.data.getBoolean("hide_duration")) {
            height += TEXT_LINE_HEIGHT;
            components++;
        }

        int computationLines = recipe.getTickInputContents(CWURecipeCapability.CAP).isEmpty() ? 0 : 1;
        if (computationLines > 0 && recipe.data.getBoolean("duration_is_total_cwu")) {
            computationLines++;
        }
        if (computationLines > 0) {
            for (IO io : IO.values()) {
                if (contentCount(recipe, CWURecipeCapability.CAP, io) == 0) continue;
                height += computationLines * TEXT_LINE_HEIGHT +
                        (computationLines - 1) * COMPUTATION_LINE_PADDING;
                components++;
            }
        }

        if (RecipeHelper.getRealEUt(recipe).voltage() > 0) {
            for (IO io : IO.values()) {
                if (contentCount(recipe, EURecipeCapability.CAP, io) == 0) continue;
                height += TEXT_LINE_HEIGHT * 2 + TEXT_COMPONENT_PADDING + 1;
                components++;
            }
        }

        if (!recipe.getInputEUt().isEmpty()) {
            height += OVERCLOCK_BUTTON_HEIGHT;
            components++;
        }
        return height + Math.max(0, components - 1) * TEXT_COMPONENT_PADDING;
    }

    private static int contentCount(GTRecipe recipe, RecipeCapability<?> capability, IO io) {
        if (io == IO.IN) {
            return recipe.getInputContents(capability).size() + recipe.getTickInputContents(capability).size();
        }
        if (io == IO.OUT) {
            return recipe.getOutputContents(capability).size() + recipe.getTickOutputContents(capability).size();
        }
        return 0;
    }

    private static GTRecipe getRecipe(GTEmiRecipe emiRecipe) {
        return RECIPE_HANDLE == null ? null : (GTRecipe) RECIPE_HANDLE.get(emiRecipe);
    }

    private static VarHandle findRecipeHandle() {
        try {
            return MethodHandles.privateLookupIn(GTEmiRecipe.class, MethodHandles.lookup())
                    .findVarHandle(GTEmiRecipe.class, "recipe", GTRecipe.class);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    private static VarHandle findGridBuildersHandle() {
        try {
            return MethodHandles.privateLookupIn(CapabilityUIInfo.class, MethodHandles.lookup())
                    .findVarHandle(CapabilityUIInfo.class, "recipeViewerLayoutGridBuilders", Map.class);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    private static Bounds bounds(int width, int height) {
        return new Bounds(0, 0, width, height);
    }

    private record LayoutMetrics(int width, int height) {}
}
