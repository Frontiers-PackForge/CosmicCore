package com.ghostipedia.cosmiccore.common.power.steam;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.gui.RecipeUIModifier;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;

import brachy.modularui.api.drawable.Text;

import java.util.List;

public final class SteamRecipeViewerModifier {

    public static final RecipeUIModifier INSTANCE = (recipe, widget) -> {
        for (Component line : summaryLines(recipe)) {
            widget.textComponents.child(Text.of(line).asWidget());
        }
    };

    private SteamRecipeViewerModifier() {}

    public static List<Component> summaryLines(GTRecipe recipe) {
        var plan = SteamRecipeExecution.resolve(recipe);
        if (plan == null) return List.of();

        var lowPressure = plan.lowPressure();
        var highPressure = plan.highPressure();
        return List.of(
                Component.translatable(
                        "cosmiccore.steam.recipe.time",
                        formatSeconds(lowPressure.durationTicks()),
                        formatSeconds(highPressure.durationTicks())),
                Component.translatable(
                        "cosmiccore.steam.recipe.flow",
                        FormattingUtil.formatNumbers(lowPressure.steamPerTick()),
                        FormattingUtil.formatNumbers(highPressure.steamPerTick())));
    }

    private static String formatSeconds(int durationTicks) {
        return FormattingUtil.formatNumbers((double) durationTicks / 20);
    }
}
