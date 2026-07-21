package com.ghostipedia.cosmiccore.gtbridge;

import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm.BloomwyrmRecipeKeys;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm.BloomwyrmSeason;

import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;
import com.gregtechceu.gtceu.api.recipe.gui.RecipeUIModifier;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;

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
        if (recipe.data.contains(BloomwyrmRecipeKeys.SEASONAL_CHARGE_INPUT)) {
            BloomwyrmSeason season = BloomwyrmRecipeKeys.favoredSeason(recipe.data);
            addLine(
                    widget,
                    "cosmiccore.bloomwyrm.recipe.seasonal_charge_input",
                    Component.translatable(season == null ?
                            "cosmiccore.bloomwyrm.essence.generic" : season.essenceTranslationKey()),
                    FormattingUtil.formatNumbers(recipe.data.getLong(BloomwyrmRecipeKeys.SEASONAL_CHARGE_INPUT)));
        }
        if (recipe.data.contains(BloomwyrmRecipeKeys.SEASONAL_CHARGE_OUTPUT)) {
            BloomwyrmSeason season = BloomwyrmRecipeKeys.favoredSeason(recipe.data);
            addLine(
                    widget,
                    "cosmiccore.bloomwyrm.recipe.seasonal_charge_output",
                    Component.translatable(season == null ?
                            "cosmiccore.bloomwyrm.essence.generic" : season.essenceTranslationKey()),
                    FormattingUtil.formatNumbers(recipe.data.getLong(BloomwyrmRecipeKeys.SEASONAL_CHARGE_OUTPUT)));
        }
        if (recipe.data.contains(BloomwyrmRecipeKeys.MAX_PARALLEL)) {
            addLine(
                    widget,
                    "cosmiccore.bloomwyrm.recipe.max_parallel",
                    FormattingUtil.formatNumbers(recipe.data.getInt(BloomwyrmRecipeKeys.MAX_PARALLEL)));
        }
    };

    public static final RecipeUIModifier FAVORED_SEASON = (recipe, widget) -> {
        BloomwyrmSeason season = BloomwyrmRecipeKeys.favoredSeason(recipe.data);
        if (season == null) return;
        addLine(
                widget,
                "cosmiccore.bloomwyrm.recipe.favored_season",
                Component.translatable(season.translationKey()));
    };

    private BloomwyrmRecipeUI() {}

    private static void addLine(GTRecipeViewerWidget widget, String key, Object... values) {
        widget.textComponents.child(new TextWidget<>(Text.lang(key, values)).color(TEXT_COLOR));
    }
}
