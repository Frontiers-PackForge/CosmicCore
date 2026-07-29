package com.ghostipedia.cosmiccore.gtbridge;

import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm.BloomwyrmRecipeKeys;

import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;
import com.gregtechceu.gtceu.api.recipe.gui.RecipeUIModifier;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.widgets.TextWidget;

public final class BloomwyrmRecipeUI {

    private static final int TEXT_COLOR = 0xFF404040;

    public static final RecipeUIModifier COMMON = (recipe, widget) -> {
        if (recipe.data.contains(BloomwyrmRecipeKeys.BIOPOWER_INPUT)) {
            addLine(
                    widget,
                    "cosmiccore.bloomwyrm.recipe.biopower_input",
                    FormattingUtil.formatNumbers(recipe.data.getInt(BloomwyrmRecipeKeys.BIOPOWER_INPUT)));
        }
        if (recipe.data.contains(BloomwyrmRecipeKeys.BIOPOWER_OUTPUT)) {
            addLine(
                    widget,
                    "cosmiccore.bloomwyrm.recipe.biopower_output",
                    FormattingUtil.formatNumbers(recipe.data.getInt(BloomwyrmRecipeKeys.BIOPOWER_OUTPUT)));
        }
        if (recipe.data.contains(BloomwyrmRecipeKeys.CHARGE_INPUT)) {
            addLine(
                    widget,
                    "cosmiccore.bloomwyrm.recipe.charge_input",
                    FormattingUtil.formatNumbers(recipe.data.getLong(BloomwyrmRecipeKeys.CHARGE_INPUT)));
        }
        if (recipe.data.contains(BloomwyrmRecipeKeys.CHARGE_OUTPUT)) {
            addLine(
                    widget,
                    "cosmiccore.bloomwyrm.recipe.charge_output",
                    FormattingUtil.formatNumbers(recipe.data.getLong(BloomwyrmRecipeKeys.CHARGE_OUTPUT)));
        }
        if (recipe.data.contains(BloomwyrmRecipeKeys.MAX_PARALLEL)) {
            addLine(
                    widget,
                    "cosmiccore.bloomwyrm.recipe.max_parallel",
                    FormattingUtil.formatNumbers(recipe.data.getInt(BloomwyrmRecipeKeys.MAX_PARALLEL)));
        }
    };

    private BloomwyrmRecipeUI() {}

    private static void addLine(GTRecipeViewerWidget widget, String key, Object... values) {
        widget.textComponents.child(new TextWidget<>(Text.lang(key, values)).color(TEXT_COLOR));
    }
}
