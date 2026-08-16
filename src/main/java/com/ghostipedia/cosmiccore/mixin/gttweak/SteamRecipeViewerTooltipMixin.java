package com.ghostipedia.cosmiccore.mixin.gttweak;

import com.ghostipedia.cosmiccore.common.power.steam.SteamRecipeExecution;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.widget.WidgetTree;
import brachy.modularui.widgets.TextWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ALLOY_SMELTER_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.BENDER_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.COMPRESSOR_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.EXTRACTOR_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.FORGE_HAMMER_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.FURNACE_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.MACERATOR_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ROCK_BREAKER_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.WIREMILL_RECIPES;

@Mixin(value = GTRecipeViewerWidget.class, remap = false)
public abstract class SteamRecipeViewerTooltipMixin {

    private static final Set<GTRecipeType> COSMICCORE$STEAM_RECIPE_TYPES = Set.of(
            EXTRACTOR_RECIPES,
            MACERATOR_RECIPES,
            COMPRESSOR_RECIPES,
            FORGE_HAMMER_RECIPES,
            FURNACE_RECIPES,
            ALLOY_SMELTER_RECIPES,
            ROCK_BREAKER_RECIPES,
            BENDER_RECIPES,
            WIREMILL_RECIPES);

    @Shadow
    private GTRecipe modifiedRecipe;

    @Inject(method = "loadContentIntoSlots", at = @At("TAIL"), require = 1)
    private void cosmiccore$appendSteamExecutionTooltip(CallbackInfo ci) {
        if (!COSMICCORE$STEAM_RECIPE_TYPES.contains(modifiedRecipe.getType())) return;

        var plan = SteamRecipeExecution.resolve(modifiedRecipe);
        if (plan == null) return;

        var widget = WidgetTree.findFirstWithNameNullable((GTRecipeViewerWidget) (Object) this, "eu");
        if (!(widget instanceof TextWidget<?> euWidget)) return;

        var highPressure = plan.highPressure();
        var lowPressure = plan.lowPressure();
        var eu = RecipeHelper.getRealEUt(modifiedRecipe);
        var tooltip = new RichTooltip();
        tooltip.addLine(Text.lang("gtceu.recipe.eu.total", FormattingUtil.formatNumbers(eu.getTotalEU()))
                .withStyle(ChatFormatting.UNDERLINE));
        tooltip.addLine(Text.lang(
                "cosmiccore.steam.recipe.high_pressure",
                FormattingUtil.formatNumbers(highPressure.steamPerTick()),
                formatSeconds(highPressure.durationTicks())));
        tooltip.addLine(Text.lang(
                "cosmiccore.steam.recipe.low_pressure",
                FormattingUtil.formatNumbers(lowPressure.steamPerTick()),
                formatSeconds(lowPressure.durationTicks())));
        euWidget.tooltip(tooltip);
    }

    private static String formatSeconds(int durationTicks) {
        return FormattingUtil.formatNumbers((double) durationTicks / 20);
    }
}
