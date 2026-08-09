package com.ghostipedia.cosmiccore.integration.emi;

import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
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
//This is such a hack, whatever
public final class GTEmiRecipeBounds {

    private static final int SLOT_SIZE = 18;
    private static final int MIN_WIDTH = 160;
    private static final int BASE_TEXT_HEIGHT = 64;
    private static final int ROOT_OVERHEAD = 14;
    private static final String DEFAULT_PROGRESS_SUPPLIER_PREFIX = GTRecipeTypeUILayout.Builder.class.getName() +
            "$$Lambda";
    private static final VarHandle RECIPE_HANDLE = findRecipeHandle();
    private static final VarHandle GRID_BUILDERS_HANDLE = findGridBuildersHandle();
    private static final Map<GTRecipeType, LayoutMetrics> LAYOUT_METRICS = new ConcurrentHashMap<>();

    private GTEmiRecipeBounds() {}

    public static void clearCache() {
        LAYOUT_METRICS.clear();
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
                !hasOnlyDefaultCapabilities(recipeType))
            return null;

        LayoutMetrics metrics = LAYOUT_METRICS.computeIfAbsent(
                recipeType, ignored -> measureLayout(recipeType, layout));
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

    private static LayoutMetrics measureLayout(GTRecipeType recipeType, GTRecipeTypeUILayout layout) {
        GridMetrics inputs = measureColumn(recipeType, IO.IN);
        GridMetrics outputs = measureColumn(recipeType, IO.OUT);
        int progressSize = Math.max(SLOT_SIZE, layout.getProgressBar().progressSize());
        int childPadding = progressSize / 2 + 2;
        int contentWidth = inputs.width() + outputs.width() + progressSize + childPadding * 2;
        int width = Math.max(MIN_WIDTH, contentWidth + 10);
        int contentHeight = Math.max(progressSize, Math.max(inputs.height(), outputs.height()));
        int height = ROOT_OVERHEAD + contentHeight + BASE_TEXT_HEIGHT;
        return new LayoutMetrics(width, height);
    }

    private static GridMetrics measureColumn(GTRecipeType recipeType, IO io) {
        int width = 0;
        int height = 0;
        for (RecipeCapability<?> capability : new RecipeCapability<?>[] {
                ItemRecipeCapability.CAP, FluidRecipeCapability.CAP
        }) {
            int capacity = recipeType.getMaxSlots(capability, io);
            if (capacity == 0) continue;
            int columns = Math.min(3, capacity);
            int rows = Math.ceilDiv(capacity, columns);
            width = Math.max(width, columns * SLOT_SIZE);
            height += rows * SLOT_SIZE;
        }
        return new GridMetrics(width, height);
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

    private record GridMetrics(int width, int height) {}

    private record LayoutMetrics(int width, int height) {}
}
